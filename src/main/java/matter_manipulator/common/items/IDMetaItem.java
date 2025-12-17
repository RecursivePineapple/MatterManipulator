package matter_manipulator.common.items;

public enum IDMetaItem {

    // Please pretty please, add your entries while conserving the order
    Hologram(0, MMItemList.Hologram),
    PowerCore0(1, MMItemList.PowerCore0),
    ComputerCore0(2, MMItemList.ComputerCore0),
    TeleporterCore0(3, MMItemList.TeleporterCore0),
    Frame0(4, MMItemList.Frame0),
    Lens0(5, MMItemList.Lens0),
    PowerCore1(6, MMItemList.PowerCore1),
    ComputerCore1(7, MMItemList.ComputerCore1),
    TeleporterCore1(8, MMItemList.TeleporterCore1),
    Frame1(9, MMItemList.Frame1),
    Lens1(10, MMItemList.Lens1),
    PowerCore2(11, MMItemList.PowerCore2),
    ComputerCore2(12, MMItemList.ComputerCore2),
    TeleporterCore2(13, MMItemList.TeleporterCore2),
    Frame2(14, MMItemList.Frame2),
    Lens2(15, MMItemList.Lens2),
    PowerCore3(16, MMItemList.PowerCore3),
    ComputerCore3(17, MMItemList.ComputerCore3),
    TeleporterCore3(18, MMItemList.TeleporterCore3),
    Frame3(19, MMItemList.Frame3),
    Lens3(20, MMItemList.Lens3),
    AEDownlink(21, MMItemList.AEDownlink),
    QuantumDownlink(22, MMItemList.QuantumDownlink),
    UpgradeBlank(23, MMItemList.UpgradeBlank),
    UpgradePowerP2P(24, MMItemList.UpgradePowerP2P),
    UpgradePrototypeMining(25, MMItemList.UpgradePrototypeMining),
    UpgradeSpeed(26, MMItemList.UpgradeSpeed),
    UpgradePowerEff(27, MMItemList.UpgradePowerEff),
    //
    ;

    public final int ID;
    public final MMItemList container;

    IDMetaItem(int ID, MMItemList container) {
        this.ID = ID;
        this.container = container;
    }
}
