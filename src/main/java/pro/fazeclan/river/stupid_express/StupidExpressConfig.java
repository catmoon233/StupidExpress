package pro.fazeclan.river.stupid_express;

import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.api.SaveType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import org.jetbrains.annotations.NotNull;

public class StupidExpressConfig extends Config {

    public StupidExpressConfig() {
        super(StupidExpress.id("config"));
    }

    public boolean necromancerHasShop = false;
    public boolean arsonistKeepsGameGoing = false;
    public boolean loversKnowImmediately = true;
    public boolean loversWinWithKillers = false;
    public boolean loversWinWithCivilians = true;

    @Override
    public int defaultPermLevel() {
        return 2;
    }

    @Override
    public @NotNull FileType fileType() {
        return FileType.JSON5;
    }

    @Override
    public @NotNull SaveType saveType() {
        return SaveType.SEPARATE;
    }
}
