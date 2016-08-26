/*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.github.mce.minigames.impl.cmd;

import java.util.Collections;
import java.util.List;

import com.github.mce.minigames.api.CommonMessages;
import com.github.mce.minigames.api.MglibInterface;
import com.github.mce.minigames.api.MinigameException;
import com.github.mce.minigames.api.cmd.CommandInterface;
import com.github.mce.minigames.api.cmd.SubCommandHandlerInterface;
import com.github.mce.minigames.api.locale.LocalizedMessageInterface;
import com.github.mce.minigames.impl.MinigamesPlugin;

/**
 * Command to display useful information.
 * 
 * @author mepeisen
 */
public class InfoCommandHandler implements SubCommandHandlerInterface
{
    
    @Override
    public void handle(CommandInterface command) throws MinigameException
    {
        // TODO Check permission
        
        if (command.getArgs().length > 0)
        {
            final String name = command.getArgs()[0].toLowerCase();
            switch (name)
            {
                case "extensions": //$NON-NLS-1$
                    new InfoExtensionsCommandHandler().handle(command.consumeArgs(1));
                    return;
                case "minigames": //$NON-NLS-1$
                    new InfoMinigamesCommandHandler().handle(command.consumeArgs(1));
                    return;
                case "arenas": //$NON-NLS-1$
                    new InfoArenasCommandHandler().handle(command.consumeArgs(1));
                    return;
                default:
                    command.send(CommonMessages.InfoUnknownSubCommand, command.getCommandPath(), name);
                    return;
            }
        }
        
        final MglibInterface lib = MglibInterface.INSTANCE.get();
        final String mode = ((MinigamesPlugin)lib).getModeString();
        final String debug = lib.debug() ? "TRUE" : "FALSE"; //$NON-NLS-1$ //$NON-NLS-2$
        command.send(CommonMessages.InfoCommandOutput,
                command.getCommandPath(),
                lib.getMinecraftVersion().name(),
                lib.getLibVersionString(),
                mode,
                debug
                );
    }
    
    @Override
    public List<String> onTabComplete(CommandInterface command, String lastArg) throws MinigameException
    {
        return Collections.emptyList();
    }
    
    @Override
    public LocalizedMessageInterface getShortDescription(CommandInterface command)
    {
        return CommonMessages.InfoCommandShortDescription;
    }
    
    @Override
    public LocalizedMessageInterface getDescription(CommandInterface command)
    {
        return CommonMessages.InfoCommandDescription;
    }
    
}
