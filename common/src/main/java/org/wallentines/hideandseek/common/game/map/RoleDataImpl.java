package org.wallentines.hideandseek.common.game.map;

import org.wallentines.hideandseek.api.game.map.RoleData;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.text.*;
import org.wallentines.midnightcore.api.module.session.AbstractSession;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoleDataImpl implements RoleData {

    private Color color;
    private MComponent name;

    private Vec3d spawnLocation;
    private Float spawnRotation;
    private MComponent properName;
    private MComponent pluralName;

    private Boolean hideName;
    private final List<String> classes = new ArrayList<>();

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public MComponent getName() {
        return name;
    }

    @Override
    public MComponent getProperName() {
        return properName;
    }

    @Override
    public MComponent getPluralName() {
        return pluralName;
    }

    @Override
    public Vec3d getSpawnLocation() {
        return spawnLocation;
    }

    @Override
    public float getSpawnRotation() {
        return spawnRotation == null ? 0.0f : spawnRotation;
    }

    @Override
    public boolean shouldHideName() {
        return hideName != null && hideName;
    }

    @Override
    public Collection<String> getClassNames() {

        return classes;
    }

    @Override
    public String getRandomClass() {
        return classes.isEmpty() ? null : classes.get(AbstractSession.RANDOM.nextInt(classes.size()));
    }

    public RoleDataImpl setSpawnLocation(Vec3d spawnLocation) {
        this.spawnLocation = spawnLocation;
        return this;
    }

    public RoleDataImpl setSpawnRotation(Float spawnRotation) {
        this.spawnRotation = spawnRotation;
        return this;
    }

    public RoleDataImpl setProperName(MComponent properName) {
        this.properName = properName;
        return this;
    }

    public RoleDataImpl setPluralName(MComponent pluralName) {
        this.pluralName = pluralName;
        return this;
    }

    public RoleDataImpl setColor(Color color) {
        this.color = color;
        return this;
    }

    public RoleDataImpl setName(MComponent name) {
        this.name = name;
        if(properName == null) properName = new MTextComponent("The ").withStyle(name.getStyle()).withChild(name.copy().withStyle(null));
        if(pluralName == null) pluralName = name.copy().withChild(new MTextComponent("s"));
        return this;
    }

    public RoleDataImpl setHideName(Boolean hideName) {
        this.hideName = hideName;
        return this;
    }

    public RoleDataImpl inheritFrom(RoleData other) {

        if(other == null) return this;

        RoleDataImpl out = new RoleDataImpl()
            .setColor(color == null ? other.getColor() : color)
            .setName(name == null ? other.getName() : name)
            .setSpawnLocation(spawnLocation == null ? other.getSpawnLocation() : spawnLocation)
            .setSpawnRotation(spawnRotation == null ? other.getSpawnRotation() : spawnRotation)
            .setPluralName(pluralName == null ? other.getPluralName() : pluralName)
            .setProperName(properName == null ? other.getProperName() : properName)
            .setHideName(hideName == null ? other.shouldHideName() : hideName);

        out.classes.addAll(classes);
        return out;
    }

    public static final Serializer<RoleDataImpl> SERIALIZER = ObjectSerializer.create(
            MComponent.SERIALIZER.entry("name", RoleDataImpl::getName).optional(),
            TextColor.SERIALIZER.entry("color", RoleDataImpl::getColor).optional(),
            Vec3d.SERIALIZER.entry("spawn_location", RoleDataImpl::getSpawnLocation).optional(),
            Serializer.FLOAT.entry("spawn_rotation", RoleDataImpl::getSpawnRotation).optional(),
            MComponent.SERIALIZER.entry("plural_name", RoleDataImpl::getPluralName).optional(),
            MComponent.SERIALIZER.entry("plural_name", RoleDataImpl::getPluralName).optional(),
            Serializer.BOOLEAN.entry("hide_name", RoleDataImpl::shouldHideName).optional(),
            Serializer.STRING.listOf().entry("classes", RoleDataImpl::getClassNames).optional(),
            (name, color, loc, rot, pluName, proName, hideName, classes) -> {

                RoleDataImpl out = new RoleDataImpl();
                if(name != null) out.setName(name);
                if(color != null) out.setColor(color);
                if(loc != null) out.setSpawnLocation(loc);
                if(rot != null) out.setSpawnRotation(rot);
                if(pluName != null) out.setPluralName(pluName);
                if(proName != null) out.setProperName(proName);
                if(hideName != null) out.setHideName(hideName);
                out.classes.addAll(classes);

                return out;
            }
    );

}
