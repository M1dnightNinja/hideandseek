package org.wallentines.hideandseek.common.game;

import org.wallentines.hideandseek.api.game.UIDisplay;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.api.text.TextColor;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.math.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UIDisplayImpl implements UIDisplay {

    private MComponent name;
    private final List<MComponent> description = new ArrayList<>();
    private TextColor color;

    private MItemStack cachedItem;

    boolean customItem = false;

    public UIDisplayImpl(MComponent name, Collection<MComponent> description, TextColor color, MItemStack is) {
        this.name = name;
        this.color = color;
        this.description.addAll(description);

        if(is == null) {
            cachedItem = generateItem();
        } else {
            cachedItem = is.copy();
            customItem = true;
        }
    }

    @Override
    public MComponent getName() {
        return name.copy();
    }

    @Override
    public Collection<MComponent> getDescription() {

        List<MComponent> out = new ArrayList<>();
        description.forEach(line -> out.add(line.copy()));

        return out;
    }

    public void setDisplayItem(MItemStack cachedItem) {
        this.cachedItem = cachedItem;
        this.customItem = true;
    }

    @Override
    public MItemStack getDisplayItem() {
        return cachedItem;
    }

    @Override
    public TextColor getColor() {
        return color;
    }

    private MItemStack generateItem() {

        return MItemStack.Builder.woolWithColor(color).withName(getName()).withLore(getDescription()).build();
    }

    public void setName(MComponent name) {
        this.name = name;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }

    public void setDescription(String desc) {

        description.clear();
        if(desc == null) return;

        MComponent comp = MComponent.parse(desc);
        splitDescription(comp, new StringBuilder(), description);

        if(!customItem) {
            cachedItem = generateItem();
        }
    }

    public MComponent getDescriptionFlattened() {

        if(description.isEmpty()) return new MTextComponent("");

        MComponent base = description.get(0);
        for(int i = 1 ; i < description.size() ; i++) {

            base.addChild(description.get(i));
        }

        return base;
    }

    private static void splitDescription(MComponent current,  StringBuilder currentLine, List<MComponent> outDescription) {

        String content = current.getContent();

        String[] words = content.split(" ");

        int lineLength = picaSize(currentLine.toString());

        for(int i = 0 ; i < words.length ; i++) {

            String s = words[i];
            if(i > 0) {
                s += ' ';
            }

            int wordSize = picaSize(s);
            lineLength += wordSize;
            if(lineLength > 22000) {
                outDescription.add(new MTextComponent(currentLine.toString()).withStyle(current.getStyle().copy()));

                currentLine.setLength(0);
                currentLine.trimToSize();

                lineLength = wordSize;
            }

            currentLine.append(s);
        }

        outDescription.add(new MTextComponent(currentLine.toString()).withStyle(current.getStyle().copy()));

        for(MComponent comp : current.getChildren()) {

            comp.getStyle().fillFrom(current.getStyle());
            splitDescription(comp, currentLine, outDescription);
        }
    }

    private static int picaSize(String s)
    {
        String lookup = " .:,;'^`!|jl/\\i-()JfIt[]?{}sr*a\"ce_gFzLxkP+0123456789<=>~qvy$SbduEphonTBCXY#VRKZN%GUAHD@OQ&wmMW";
        int result = 0;
        for (int i = 0; i < s.length(); ++i)
        {
            int c = lookup.indexOf(s.charAt(i));
            result += (c < 0 ? 60 : c) * 7 + 200;
        }
        return result;
    }


    public static final ConfigSerializer<UIDisplayImpl> SERIALIZER = ConfigSerializer.create(
            MComponent.INLINE_SERIALIZER.entry("name", UIDisplayImpl::getName),
            MComponent.INLINE_SERIALIZER.listOf().entry("description", UIDisplayImpl::getDescription).optional(),
            TextColor.SERIALIZER.entry("color", UIDisplayImpl::getColor).orDefault(new TextColor(Color.WHITE)),
            MItemStack.SERIALIZER.entry("item", UIDisplayImpl::getDisplayItem).optional(),
            UIDisplayImpl::new
    );

    public static UIDisplayImpl createDefault(String id) {
        return new UIDisplayImpl(new MTextComponent(id), new ArrayList<>(), new TextColor(Color.WHITE), null);
    }

}
