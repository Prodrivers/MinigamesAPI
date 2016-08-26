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

import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.mce.minigames.api.cmd.CommandHandlerInterface;

/**
 * Basic interface for providers, either minigame plugins or extensions.
 * 
 * @author mepeisen
 */
public interface CommonProviderInterface
{
    
    /**
     * Returns the java plugin that creates the component.
     * 
     * @return java plugin.
     */
    JavaPlugin getJavaPlugin();
    
    /**
     * Returns the messages classes for predefined messages.
     * 
     * <p>
     * Simple return {@code null} if you only use the default messages from minigames library.
     * </p>
     * 
     * @return message classes for predefined messages.
     */
    Iterable<Class<? extends Enum<?>>> getMessageClasses();
    
    /**
     * Returns the permission classes.
     * 
     * <p>
     * Simple return {@code null} if you only use the default permissions from minigames library.
     * </p>
     * 
     * @return permission classes for predefined permissions.
     */
    Iterable<Class<? extends Enum<?>>> getPermissions();
    
    /**
     * Returns the bukkit (main) commands registered by this minigame.
     * 
     * <p>
     * Simply return {@code null} if there are no additional bukkit commands to register.
     * </p>
     * 
     * @return bukkit (main) commands.
     */
    Map<String, CommandHandlerInterface> getBukkitCommands();
    
    /**
     * Returns the configuration classes.
     * 
     * <p>
     * Simple return {@code null} if you only use the default configuration options from minigames library.
     * </p>
     * 
     * @return configuration classes for predefined configurations.
     */
    Iterable<Class<? extends Enum<?>>> getConfigurations();
    
}
