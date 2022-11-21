package org.wallentines.hideandseek.common.util;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.Lobby;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class GUIUtil {

    public static InventoryGUI lobbyGUI(Consumer<Lobby> choice, Function<Lobby, Boolean> filter, boolean showPlayerCount, MPlayer player) {

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) throw new IllegalStateException("Attempt to create GUI before MidnightCoreAPI was created!");

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
        InventoryGUI gui = api.createGUI(prov.getMessage("gui.lobby.title", player));

        PagedGUI paged = new PagedGUI(gui);
        int index = 0;
        for (Lobby lobby : ContentRegistryImpl.INSTANCE.getLobbies()) {

            if (!filter.apply(lobby)) continue;

            MItemStack is = lobby.getDisplay().getDisplayItem().copy();

            if(showPlayerCount) {
                List<MComponent> lore = is.getLore();
                MComponent count = prov.getMessage("gui.lobby.player_count", player, lobby, HideAndSeekAPI.getInstance().getSessionManager().getLobbySession(lobby));
                lore.add(count);
                is.setLore(lore);
            }

            paged.setItem(index++, is, (type, clicker) -> {
                choice.accept(lobby);
                gui.close(clicker);
            });
        }
        paged.addPageButtons(
            MItemStack.Builder.of(new Identifier("minecraft", "lime_stained_glass_pane")).withName(prov.getMessage("gui.next_page", player)).build(),
            MItemStack.Builder.of(new Identifier("minecraft", "red_stained_glass_pane")).withName(prov.getMessage("gui.prev_page", player)).build()
        );

        return paged.handle;
    }

    public static InventoryGUI mapGUI(Consumer<Map> choice, Function<Map, Boolean> filter, MPlayer player) {

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) throw new IllegalStateException("Attempt to create GUI before MidnightCoreAPI was created!");

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
        InventoryGUI gui = api.createGUI(prov.getMessage("gui.map.title", player));

        PagedGUI paged = new PagedGUI(gui);
        int index = 0;
        for(Map map : ContentRegistryImpl.INSTANCE.getMaps()) {

            if(!filter.apply(map)) continue;
            paged.setItem(index, map.getDisplay().getDisplayItem(), (type, clicker) -> {
                choice.accept(map);
                gui.close(clicker);
            });
            index++;
        }

        paged.addPageButtons(
            MItemStack.Builder.of(new Identifier("minecraft", "lime_stained_glass_pane")).withName(prov.getMessage("gui.next_page", player)).build(),
            MItemStack.Builder.of(new Identifier("minecraft", "red_stained_glass_pane")).withName(prov.getMessage("gui.prev_page", player)).build()
        );

        return paged.handle;
    }

    public static InventoryGUI playerGUI(Consumer<MPlayer> choice, Function<MPlayer, Boolean> filter, MPlayer player) {

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) throw new IllegalStateException("Attempt to create GUI before MidnightCoreAPI was created!");

        LangProvider prov = HideAndSeekAPI.getInstance().getLangProvider();
        InventoryGUI gui = api.createGUI(prov.getMessage("gui.player.title", player));

        PagedGUI paged = new PagedGUI(gui);
        int index = 0;
        for(MPlayer mpl : MidnightCoreAPI.getInstance().getPlayerManager()) {

            if(!filter.apply(mpl)) continue;

            MItemStack is = MItemStack.Builder.headWithSkin(mpl.getSkin()).withName(mpl.getName()).build();

            paged.setItem(index, is, (type, clicker) -> {
                choice.accept(mpl);
                gui.close(clicker);
            });
            index++;
        }
        paged.addPageButtons(
            MItemStack.Builder.of(new Identifier("minecraft", "lime_stained_glass_pane")).withName(prov.getMessage("gui.next_page", player)).build(),
            MItemStack.Builder.of(new Identifier("minecraft", "red_stained_glass_pane")).withName(prov.getMessage("gui.prev_page", player)).build()
        );

        return paged.handle;
    }

    public static class PagedGUI {

        private final InventoryGUI handle;
        private final int pageSize;
        private final int rawPageSize;

        private int elements;

        public PagedGUI(InventoryGUI handle) {
            this(handle, handle.getPageSize() * 9);
        }

        public PagedGUI(InventoryGUI handle, int pageSize) {

            this.handle = handle;
            this.rawPageSize = handle.getPageSize() * 9;
            this.pageSize = Math.min(rawPageSize - 9, pageSize);

            if(pageSize <= 0) throw new IllegalArgumentException("Inventory is too small to make into a paged GUI!");
        }

        public void setItem(int index, MItemStack is, InventoryGUI.GUIAction onClick) {

            int slot = (index % pageSize) + (rawPageSize * (index / pageSize));
            handle.setItem(slot, is, onClick);

            elements = Math.max(index, elements);
        }

        public void addPageButtons(MItemStack nextItem, MItemStack prevItem) {

            int pages = (int) Math.ceil(elements / (float) pageSize);

            for(int i = 0 ; i < pages ; i++) {

                if(i > 0) {

                    int slot = (rawPageSize * i) + pageSize;
                    int prevPage = i - 1;
                    handle.setItem(slot, prevItem.copy(), (type, pl) -> handle.open(pl, prevPage));
                }
                if(i < pages - 1) {

                    int slot = (rawPageSize * i) + pageSize + 8;
                    int nextPage = i + 1;
                    handle.setItem(slot, nextItem.copy(), (type, pl) -> handle.open(pl, nextPage));
                }
            }
        }
    }

}
