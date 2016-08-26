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

package com.github.mce.minigames.api;

import java.io.Serializable;
import java.util.logging.Logger;

import com.github.mce.minigames.api.arena.ArenaInterface;
import com.github.mce.minigames.api.arena.ArenaTypeDeclarationInterface;
import com.github.mce.minigames.api.config.ConfigInterface;
import com.github.mce.minigames.api.locale.MessagesConfigInterface;

/**
 * The plugin to access minigames.
 * 
 * @author mepeisen
 */
public interface MinigameInterface extends ConfigInterface
{
    
    /**
     * Returns the technical name of the minigame.
     * 
     * <p>
     * Will be the name returned by {@link PluginProviderInterface#getName()}.
     * </p>
     * 
     * @return minigame name.
     */
    String getName();
    
    /**
     * Returns the file configuration for the messages.
     * 
     * @return file configuration for messages.
     */
    MessagesConfigInterface getMessages();
    
    /**
     * Returns the type of arenas declared for this minigame
     * 
     * @return arena types.
     */
    Iterable<ArenaTypeDeclarationInterface> getDeclaredTypes();
    
    /**
     * Returns all declared arenas within this minigame.
     * 
     * @return all minigame arenas.
     */
    Iterable<ArenaInterface> getArenas();
    
    /**
     * Returns the minigame arena with given internal name.
     * 
     * @param name
     * @return arena or {@code null} if the arena does not exist.
     */
    ArenaInterface getArenas(String name);
    
    /**
     * Returns a logger for the library.
     * 
     * @return logger instance.
     */
    Logger getLogger();
    
    /**
     * Returns the short description of the minigame
     * 
     * @return minigame short description
     */
    Serializable getShortDescription();
    
    /**
     * Returns the long description of the minigame
     * 
     * @return minigame long description (multi line)
     */
    Serializable getLongDescription();
    
}
