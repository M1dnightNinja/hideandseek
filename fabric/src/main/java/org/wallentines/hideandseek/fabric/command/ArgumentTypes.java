package org.wallentines.hideandseek.fabric.command;


import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.wallentines.midnightcore.fabric.command.ServerSideArgumentInfo;
import org.wallentines.midnightcore.fabric.mixin.AccessorArgumentTypeInfos;

public class ArgumentTypes {

    public static String getSingleArg(StringReader reader) {

        int beginning = reader.getCursor();
        if (!reader.canRead()) {
            reader.skip();
        }

        while(reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }

        return reader.getString().substring(beginning, reader.getCursor());
    }

    public static void register() {

        AccessorArgumentTypeInfos.byClassMap().put(MapArgumentType.class, ServerSideArgumentInfo.string(StringArgumentType.StringType.SINGLE_WORD, MapArgumentType::any));
        AccessorArgumentTypeInfos.byClassMap().put(LobbyArgumentType.class, ServerSideArgumentInfo.string(StringArgumentType.StringType.SINGLE_WORD, LobbyArgumentType::any));
    }


}
