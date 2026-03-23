package matter_manipulator.common.utils.deps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import matter_manipulator.common.utils.DataUtils;

public class DependencyGraph<T> implements IDependencyGraph<T> {

    interface Entity {}

    @Desugar
    private record ObjectEntity<T>(T value) implements Entity { }

    @Desugar
    private record TargetEntity() implements Entity { }

    @Desugar
    private record SubgraphEntity<T>(DependencyGraph<T> graph) implements Entity { }

    @Desugar
    private record DepInfo(String dependency, boolean optional) {

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof DepInfo depInfo)) return false;

            return Objects.equals(dependency, depInfo.dependency);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(dependency);
        }
    }

    private final Object2ObjectOpenHashMap<String, Entity> entities = new Object2ObjectOpenHashMap<>();

    private final Multimap<String, DepInfo> dependencies = MultimapBuilder.hashKeys()
        .hashSetValues()
        .build();

    private final T[] zeroSized;
    private T[] cachedSorted;

    public DependencyGraph(T[] zeroSized) {
        this.zeroSized = zeroSized;
        entities.defaultReturnValue(null);
    }

    @Desugar
    private record SubgraphLocation<T>(DependencyGraph<T> graph, String name) { }

    private SubgraphLocation<T> getSubgraphLocation(String name) {
        String[] path = name.split("/");

        DependencyGraph<T> graph = this;

        for (int i = 0; i < path.length - 1; i++) {
            // Hacky way to invalidate the cache without needing two iterations.
            // This doesn't really belong here, but it's where I'm shoving it.
            graph.cachedSorted = null;

            String chunk = path[i];

            Entity e = graph.entities.get(chunk);

            if (e == null) {
                throw new MissingEntityException("Could not find subgraph: " + DataUtils.join("/", DataUtils.slice(path, 0, i)));
            }

            if (!(e instanceof DependencyGraph.SubgraphEntity<?> subgraph)) {
                throw new MissingEntityException("Entity was not subgraph: " + DataUtils.join("/", DataUtils.slice(path, 0, i)) + " (was " + e + ")");
            }

            //noinspection unchecked
            graph = (DependencyGraph<T>) subgraph.graph;
        }

        graph.cachedSorted = null;

        return new SubgraphLocation<>(graph, path[path.length - 1]);
    }

    @Override
    public void addObject(String name, T value, String... deps) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");

        var loc = getSubgraphLocation(name);

        loc.graph.entities.put(loc.name, new ObjectEntity<>(value));

        for (String dep : deps) {
            loc.graph.addUnparsedDependency(loc.name, dep);
        }
    }

    public void addUnparsedDependency(String object, String dep) {
        Objects.requireNonNull(object, "object must not be null");
        Objects.requireNonNull(dep, "dep must not be null");

        boolean optional = dep.endsWith("?");

        if (optional) {
            dep = dep.substring(0, dep.length() - 1);
        }

        if (dep.startsWith(REQUIRES)) {
            dep = dep.substring(REQUIRES.length())
                .trim();
            dependencies.put(object, new DepInfo(dep, optional));
        } else if (dep.startsWith(REQUIRED_BY)) {
            dep = dep.substring(REQUIRED_BY.length())
                .trim();
            dependencies.put(dep, new DepInfo(object, optional));
        } else if (dep.startsWith(AFTER)) {
            dep = dep.substring(AFTER.length())
                .trim();
            dependencies.put(object, new DepInfo(dep, true));
        } else if (dep.startsWith(BEFORE)) {
            dep = dep.substring(BEFORE.length())
                .trim();
            dependencies.put(dep, new DepInfo(object, true));
        } else {
            throw new IllegalArgumentException("Invalid dependency specification for object '" + object + "': '" + dep + "'");
        }

        cachedSorted = null;
    }

    @Override
    public void addDependency(String object, String dependsOn, boolean optional) {
        Objects.requireNonNull(object, "object must not be null");
        Objects.requireNonNull(dependsOn, "dependsOn must not be null");

        var loc = getSubgraphLocation(object);

        loc.graph.dependencies.put(loc.name, new DepInfo(dependsOn, optional));
    }

    @Override
    public boolean removeDependency(String object, String dependsOn) {
        Objects.requireNonNull(object, "object must not be null");
        Objects.requireNonNull(dependsOn, "dependsOn must not be null");

        var loc = getSubgraphLocation(object);

        return loc.graph.dependencies.remove(loc.name, new DepInfo(dependsOn, false));
    }

    @Override
    public void addTarget(String targetName, String... deps) {
        Objects.requireNonNull(targetName, "targetName must not be null");

        var loc = getSubgraphLocation(targetName);

        loc.graph.entities.put(loc.name, new TargetEntity());

        for (String dep : deps) {
            loc.graph.addUnparsedDependency(loc.name, dep);
        }
    }

    @Override
    public void addSubgraph(String graphName, String... deps) {
        var loc = getSubgraphLocation(graphName);

        loc.graph.entities.put(loc.name, new SubgraphEntity<>(new DependencyGraph<>(zeroSized)));

        for (String dep : deps) {
            loc.graph.addUnparsedDependency(loc.name, dep);
        }
    }

    public T[] sorted() {
        if (cachedSorted != null) return cachedSorted;

        ObjectLinkedOpenHashSet<String> path = new ObjectLinkedOpenHashSet<>();

        for (String node : dependencies.keys()) {
            preventCyclicDeps(node, false, path);
        }

        @SuppressWarnings("rawtypes")
        List out = new ArrayList<>();
        ObjectLinkedOpenHashSet<String> added = new ObjectLinkedOpenHashSet<>();

        ObjectLinkedOpenHashSet<String> remaining = new ObjectLinkedOpenHashSet<>(entities.keySet());
        while (!remaining.isEmpty()) {
            Iterator<String> iter = remaining.iterator();

            iterdeps: while (iter.hasNext()) {
                String curr = iter.next();

                for (DepInfo dep : dependencies.get(curr)) {
                    if (!added.contains(dep.dependency)) {
                        continue iterdeps;
                    }
                }

                iter.remove();

                added.add(curr);

                Entity value = entities.get(curr);

                if (value == null) {
                    throw new MissingEntityException("Could not find entity: " + curr);
                }

                if (value instanceof DependencyGraph.ObjectEntity<?> obj) {
                    //noinspection unchecked
                    out.add(obj.value);
                } else if (value instanceof DependencyGraph.SubgraphEntity<?> subgraph) {
                    //noinspection unchecked
                    out.addAll(Arrays.asList(subgraph.graph.sorted()));
                }
            }
        }

        //noinspection unchecked
        cachedSorted = ((List<T>) out).toArray(zeroSized);

        return cachedSorted;
    }

    private void preventCyclicDeps(String node, boolean optional, Set<String> path) {
        if (path.contains(node)) {
            throw new IllegalStateException(
                node + " has a cyclic dependency with itself. The path is: "
                    + path.stream()
                        .reduce("", (s, s2) -> s + ", " + s2));
        }

        if (!optional && !entities.containsKey(node)) {
            throw new IllegalStateException(
                node + " is present in the dependency graph but does not have a matching object");
        }

        path.add(node);

        for (DepInfo dep : dependencies.get(node)) {
            preventCyclicDeps(dep.dependency, dep.optional, path);
        }

        path.remove(node);
    }
}
