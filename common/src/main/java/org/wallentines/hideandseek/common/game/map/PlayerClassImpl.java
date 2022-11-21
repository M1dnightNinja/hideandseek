package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.event.ClassApplyEvent;
import org.wallentines.hideandseek.api.game.UIDisplay;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.api.game.map.PlayerClass;
import org.wallentines.hideandseek.api.game.map.Role;
import org.wallentines.hideandseek.common.Constants;
import org.wallentines.hideandseek.common.HideAndSeekImpl;
import org.wallentines.hideandseek.common.core.ContentRegistryImpl;
import org.wallentines.hideandseek.common.game.UIDisplayImpl;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MStyle;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.PrimitiveSerializers;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.*;

public class PlayerClassImpl implements PlayerClass {

    private final String id;
    private final UIDisplayImpl display;

    protected HashMap<Identifier, Integer> effects;
    protected HashMap<String, MItemStack> equipment;
    protected List<MItemStack> items;

    protected ConfigSection extraData;
    private boolean tagImmune;

    private final HashMap<Role, String> equivalencies = new HashMap<>();

    public PlayerClassImpl(String id, UIDisplayImpl display) {
        this.id = id;
        this.display = display;

        this.effects = new HashMap<>();
        this.equipment = new HashMap<>();
        this.items = new ArrayList<>();
        this.extraData = new ConfigSection();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public UIDisplay getDisplay() {
        return display;
    }

    @Override
    public Collection<MItemStack> getItems() {
        return items;
    }

    @Override
    public MItemStack getEquipmentItem(String slot) {
        return equipment.get(slot);
    }

    private void setEquipmentItem(String slot, MItemStack item) {
        equipment.put(slot, item);
    }

    @Override
    public int getEffectLevel(Identifier id) {
        return effects.getOrDefault(id, -1);
    }

    @Override
    public Collection<Identifier> getEffects() {
        return effects.keySet();
    }

    @Override
    public boolean isTagImmune() {
        return tagImmune;
    }

    @Override
    public PlayerClass getEquivalent(Map map, Role role) {
        return map.getOrGlobal(equivalencies.get(role));
    }

    @Override
    public void apply(MPlayer player) {
        ClassApplyEvent ev = new ClassApplyEvent(player, this);
        Event.invoke(ev);

        if(!ev.isCancelled()) {
            HideAndSeekImpl.applyClass(player, this);
        }
    }

    @Override
    public ConfigSection getExtraData() {
        return extraData;
    }

    public static final ConfigSerializer<PlayerClassImpl> SERIALIZER = ConfigSerializer.create(
            PrimitiveSerializers.STRING.entry("id", PlayerClassImpl::getId),
            UIDisplayImpl.SERIALIZER.<PlayerClassImpl>entry("display", cl -> cl.display).optional(),
            MItemStack.SERIALIZER.listOf().entry("items", PlayerClassImpl::getItems).optional(),
            MItemStack.SERIALIZER.mapOf().<PlayerClassImpl>entry("equipment", pc -> pc.equipment).optional(),
            PrimitiveSerializers.INT.mapOf(Constants.ID_SERIALIZER).<PlayerClassImpl>entry("effects", cl -> cl.effects).optional(),
            PrimitiveSerializers.STRING.mapOf(ContentRegistryImpl.REGISTERED_ROLE).<PlayerClassImpl>entry("equivalencies", cl -> cl.equivalencies).optional(),
            PrimitiveSerializers.BOOLEAN.entry("tag_immune", PlayerClassImpl::isTagImmune).orDefault(false),
            ConfigSerializer.RAW.entry("extra_data", PlayerClassImpl::getExtraData).orDefault(new ConfigSection()),
            (id, display, items, equipment, effects, equivalencies, tagImmune, extraData) -> {

                if(display == null) display = UIDisplayImpl.createDefault(id);
                PlayerClassImpl out = new PlayerClassImpl(id, display);

                out.items.addAll(items);
                out.equipment.putAll(equipment);
                out.effects.putAll(effects);
                out.equivalencies.putAll(equivalencies);
                out.tagImmune = tagImmune;
                out.extraData = extraData;

                return out;
            }

    );

    public static void registerPlaceholders(PlaceholderManager manager) {

        Constants.registerInlinePlaceholder(manager, "class_id", PlaceholderSupplier.create(PlayerClassImpl.class, PlayerClassImpl::getId));
        Constants.registerPlaceholder(manager, "class_name", PlaceholderSupplier.create(PlayerClassImpl.class, pc -> pc.getDisplay().getName(),
                () -> new MTextComponent("None").withStyle(new MStyle().withColor(Color.fromRGBI(7)))));

    }
}
