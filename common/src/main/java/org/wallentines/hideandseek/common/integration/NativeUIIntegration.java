package org.wallentines.hideandseek.common.integration;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.Lobby;
import org.wallentines.hideandseek.api.game.UIDisplay;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.util.GUIUtil;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.extension.ServerExtensionModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.nativeui.CustomImage;
import org.wallentines.nativeui.CustomMenu;
import org.wallentines.nativeui.NativeUIExtension;
import org.wallentines.nativeui.control.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class NativeUIIntegration {

    private static NativeUIExtension getExtension(MPlayer player) {

        ServerExtensionModule module = MidnightCoreAPI.getModule(ServerExtensionModule.class);
        if(module == null || !module.playerHasExtension(player, NativeUIExtension.ID)) return null;

        return module.getExtension(NativeUIExtension.class);
    }

    private static HoverGrid fromDisplay(UIDisplay display, CustomMenu menu, int x, int y, String clickEvent, List<String> loadedFiles) {

        Tooltip tt = new Tooltip(menu);
        tt.addChild(new Label(menu, 0, 0, null, display.getName().toConfigText()));


        int i = 14;
        for(MComponent comp : display.getDescription()) {
            tt.addChild(new Label(menu, 0, i, null, comp.toConfigText()));
            i += 10;
        }

        HoverGrid hg = new HoverGrid(menu, x, y, null, 24, 24, false);

        Image normal = new Image(menu, 0, 0, null, BuiltinControls.UI_LOCATION, BuiltinControls.SQUARE_BUTTON.width, BuiltinControls.SQUARE_BUTTON.height, BuiltinControls.SQUARE_BUTTON.u, BuiltinControls.SQUARE_BUTTON.v);

        if(clickEvent == null) {
            hg.addChild(normal);
        } else {
            Image hover = new Image(menu, 0, 0, null, BuiltinControls.UI_LOCATION, BuiltinControls.SQUARE_BUTTON_HOVER.width, BuiltinControls.SQUARE_BUTTON_HOVER.height, BuiltinControls.SQUARE_BUTTON_HOVER.u, BuiltinControls.SQUARE_BUTTON_HOVER.v);
            hg.addNormalOnlyChild(normal);
            hg.addHoverOnlyChild(hover);
            hg.onClick(clickEvent);
        }

        if(display.getCustomIcon() == null) {

            hg.addChild(new ItemIcon(menu, 4, 4, null, display.getDisplayItem()));

        } else {

            UIDisplay.CustomIconData ci = display.getCustomIcon();

            String file = ci.imageFile;
            int index = loadedFiles.indexOf(file);
            if(index == -1) {
                index = loadedFiles.size();

                File f = HideAndSeekAPI.getInstance().getDataFolder().toPath().resolve(file).toFile();
                CustomImage img = CustomImage.load(f);

                menu.addImage(img);
                loadedFiles.add(file);
            }

            Identifier imageLoc = Image.generateCustomImageId(index);
            hg.addChild(new Image(menu, 4, 4, null, imageLoc, 16, 16, ci.x, ci.y, ci.width, ci.height));
        }

        hg.addHoverOnlyChild(tt);

        return hg;
    }

    public static void openLobbyGUI(Consumer<Lobby> choice, Function<Lobby, Boolean> filter, boolean showPlayerCount, MPlayer player) {

        NativeUIExtension ext = getExtension(player);
        if(ext == null) {
            GUIUtil.lobbyGUI(choice, filter, showPlayerCount, player).open(player, 0);
            return;
        }

        CustomMenu menu = BuiltinControls.basicMenu();
        Grid grid = (Grid) menu.getRoot();

        MComponent msg = HideAndSeekAPI.getInstance().getLangProvider().getMessage("gui.lobby.title", player);
        grid.addChild(new Label(menu, 8, -12, null, msg.toConfigText()));

        ScrollView scroll = grid.addChild(BuiltinControls.scrollView(menu, BuiltinControls.BACKGROUND_INNER.u, BuiltinControls.BACKGROUND_INNER.v, BuiltinControls.BACKGROUND_INNER.width, BuiltinControls.BACKGROUND_INNER.height, 8));

        int index = 0;
        List<String> loadedImages = new ArrayList<>();
        for(Lobby lobby : ContentRegistryImpl.INSTANCE.getLobbies()) {
            if(!filter.apply(lobby)) continue;

            String id = lobby.getId();
            int offset = 32 * index;

            scroll.addChild(fromDisplay(lobby.getDisplay(), menu, 8, offset + 4, null, loadedImages));

            int nameOffset = 14;
            if(showPlayerCount) {
                nameOffset = 6;
                MComponent count = HideAndSeekAPI.getInstance().getLangProvider().getMessage("gui.lobby.player_count", player, lobby, HideAndSeekAPI.getInstance().getSessionManager().getLobbySession(lobby));
                scroll.addChild(new Label(menu, 36, offset + 18, null, count.toConfigText()));
            }
            scroll.addChild(new Label(menu, 36, offset + nameOffset, null, lobby.getDisplay().getName().toConfigText()));

            MinecraftButton btn = new MinecraftButton(menu, BuiltinControls.BACKGROUND_INNER.width - 56, offset + 6, null, 48, "Join");
            btn.onClick(id);

            scroll.addChild(btn);

            menu.setClickAction(id, mpl -> {
                ext.closeMenu(mpl);
                choice.accept(lobby);
            });

            index++;
        }

        ext.openMenu(player, menu);
    }

    public static void openMapGUI(Consumer<Map> choice, Function<Map, Boolean> filter, MPlayer player) {

        NativeUIExtension ext = getExtension(player);
        if(ext == null) {
            GUIUtil.mapGUI(choice, filter, player).open(player, 0);
            return;
        }

        CustomMenu menu = BuiltinControls.basicMenu();
        Grid grid = (Grid) menu.getRoot();

        MComponent msg = HideAndSeekAPI.getInstance().getLangProvider().getMessage("gui.map.title", player);
        grid.addChild(new Label(menu, 8, -12, null, msg.toConfigText()));

        ScrollView scroll = grid.addChild(BuiltinControls.scrollView(menu, BuiltinControls.BACKGROUND_INNER.u, BuiltinControls.BACKGROUND_INNER.v, BuiltinControls.BACKGROUND_INNER.width, BuiltinControls.BACKGROUND_INNER.height, 8));

        int row = 0;
        int column = 0;
        List<String> loadedImages = new ArrayList<>();

        for(Map map : ContentRegistryImpl.INSTANCE.getMaps()) {
            if(!filter.apply(map)) continue;

            String id = map.getId();
            int rowOffset = 32 * row;
            int columnOffset = 32 * column;

            scroll.addChild(fromDisplay(map.getDisplay(), menu, columnOffset + 12, rowOffset + 12, id, loadedImages));

            menu.setClickAction(id, mpl -> mpl.getServer().submit(() -> {
                ext.closeMenu(mpl);
                choice.accept(map);
            }));

            column++;
            if(column > 5) {
                column = 0;
                row++;
            }
        }

        ext.openMenu(player, menu);
    }

}
