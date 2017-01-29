/*
    Copyright 2016 by minigameslib.de
    All rights reserved.
    If you do not own a hand-signed commercial license from minigames.de
    you are not allowed to use this software in any way except using
    GPL (see below).

------

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

package de.minigameslib.mgapi.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import de.minigameslib.mclib.api.CommonMessages;
import de.minigameslib.mclib.api.McException;
import de.minigameslib.mclib.api.McLibInterface;
import de.minigameslib.mclib.api.cmd.CommandImpl;
import de.minigameslib.mclib.api.enums.EnumServiceInterface;
import de.minigameslib.mclib.api.objects.ComponentTypeId;
import de.minigameslib.mclib.api.objects.McPlayerInterface;
import de.minigameslib.mclib.api.objects.ObjectServiceInterface;
import de.minigameslib.mclib.api.objects.SignTypeId;
import de.minigameslib.mclib.api.objects.ZoneTypeId;
import de.minigameslib.mclib.api.util.function.McBiFunction;
import de.minigameslib.mclib.api.util.function.McSupplier;
import de.minigameslib.mgapi.api.ExtensionInterface;
import de.minigameslib.mgapi.api.ExtensionProvider;
import de.minigameslib.mgapi.api.LibState;
import de.minigameslib.mgapi.api.MinigameInterface;
import de.minigameslib.mgapi.api.MinigameProvider;
import de.minigameslib.mgapi.api.MinigamesLibInterface;
import de.minigameslib.mgapi.api.arena.ArenaInterface;
import de.minigameslib.mgapi.api.arena.ArenaTypeInterface;
import de.minigameslib.mgapi.api.events.ArenaCreateEvent;
import de.minigameslib.mgapi.api.events.ArenaCreatedEvent;
import de.minigameslib.mgapi.api.events.ArenaDeleteEvent;
import de.minigameslib.mgapi.api.events.ArenaDeletedEvent;
import de.minigameslib.mgapi.api.events.ArenaPlayerJoinEvent;
import de.minigameslib.mgapi.api.events.ArenaPlayerJoinSpectatorsEvent;
import de.minigameslib.mgapi.api.events.ArenaPlayerJoinedEvent;
import de.minigameslib.mgapi.api.events.ArenaPlayerJoinedSpectatorsEvent;
import de.minigameslib.mgapi.api.events.ArenaPlayerLeftEvent;
import de.minigameslib.mgapi.api.events.ArenaPlayerLeftSpectatorsEvent;
import de.minigameslib.mgapi.api.obj.ArenaComponentHandler;
import de.minigameslib.mgapi.api.obj.ArenaSignHandler;
import de.minigameslib.mgapi.api.obj.ArenaZoneHandler;
import de.minigameslib.mgapi.api.obj.BasicComponentTypes;
import de.minigameslib.mgapi.api.obj.BasicSignTypes;
import de.minigameslib.mgapi.api.obj.BasicZoneTypes;
import de.minigameslib.mgapi.api.player.ArenaPlayerInterface;
import de.minigameslib.mgapi.api.rules.ArenaRuleSetInterface;
import de.minigameslib.mgapi.api.rules.ArenaRuleSetType;
import de.minigameslib.mgapi.api.rules.BasicArenaRuleSets;
import de.minigameslib.mgapi.api.rules.BasicComponentRuleSets;
import de.minigameslib.mgapi.api.rules.BasicSignRuleSets;
import de.minigameslib.mgapi.api.rules.BasicZoneRuleSets;
import de.minigameslib.mgapi.api.rules.ComponentRuleSetInterface;
import de.minigameslib.mgapi.api.rules.ComponentRuleSetType;
import de.minigameslib.mgapi.api.rules.RuleSetType;
import de.minigameslib.mgapi.api.rules.SignRuleSetInterface;
import de.minigameslib.mgapi.api.rules.SignRuleSetType;
import de.minigameslib.mgapi.api.rules.ZoneRuleSetInterface;
import de.minigameslib.mgapi.api.rules.ZoneRuleSetType;
import de.minigameslib.mgapi.impl.MglibMessages.MglibCoreErrors;
import de.minigameslib.mgapi.impl.arena.ArenaImpl;
import de.minigameslib.mgapi.impl.arena.ArenaPlayerImpl;
import de.minigameslib.mgapi.impl.arena.ArenaPlayerPersistentData;
import de.minigameslib.mgapi.impl.cmd.Mg2Command;
import de.minigameslib.mgapi.impl.internal.TaskManager;
import de.minigameslib.mgapi.impl.obj.BattleZone;
import de.minigameslib.mgapi.impl.obj.EmptyComponent;
import de.minigameslib.mgapi.impl.obj.EmptySign;
import de.minigameslib.mgapi.impl.obj.EmptyZone;
import de.minigameslib.mgapi.impl.obj.GenericComponent;
import de.minigameslib.mgapi.impl.obj.GenericSign;
import de.minigameslib.mgapi.impl.obj.GenericZone;
import de.minigameslib.mgapi.impl.obj.JoinSign;
import de.minigameslib.mgapi.impl.obj.JoinZone;
import de.minigameslib.mgapi.impl.obj.LeaveSign;
import de.minigameslib.mgapi.impl.obj.LeaveZone;
import de.minigameslib.mgapi.impl.obj.LobbyZone;
import de.minigameslib.mgapi.impl.obj.MainZone;
import de.minigameslib.mgapi.impl.obj.SpawnComponent;
import de.minigameslib.mgapi.impl.obj.SpectatorZone;
import de.minigameslib.mgapi.impl.rules.BasicMatch;
import de.minigameslib.mgapi.impl.tasks.InitTask;

/**
 * Implementation of minigames plugin
 * 
 * @author mepeisen
 */
public class MinigamesPlugin extends JavaPlugin implements MinigamesLibInterface
{
    
    /**
     * the current library state.
     */
    private LibState                                                                                                              state                 = LibState.Initializing;
    
    /**
     * the registered minigames per plugin
     */
    private Map<String, MinigameImpl>                                                                                             minigamesPerPlugin    = new HashMap<>();
    
    /**
     * the registered minigames per name
     */
    private Map<String, MinigameImpl>                                                                                             minigamesPerName      = new TreeMap<>();
    
    /**
     * the registered extensions per plugin
     */
    private Map<String, ExtensionImpl>                                                                                            extensionsPerPlugin   = new HashMap<>();
    
    /**
     * the registered extensions per name
     */
    private Map<String, ExtensionImpl>                                                                                            extensionsPerName     = new TreeMap<>();
    
    /** the console commands. */
    private Mg2Command                                                                                                            mg2Command            = new Mg2Command();
    
    /** arenas per name. */
    private Map<String, ArenaImpl>                                                                                                arenasPerName         = new TreeMap<>();
    
    /**
     * The rule sets per plugin.
     */
    private final Map<String, Set<RuleSetType>>                                                                                   ruleSetsPerPlugin     = new HashMap<>();
    
    /**
     * The creator func by arena rule set type
     */
    private final Map<ArenaRuleSetType, McBiFunction<ArenaRuleSetType, ArenaInterface, ArenaRuleSetInterface>>                    arenaRuleSetTypes     = new HashMap<>();
    
    /**
     * The creator func by zone rule set type
     */
    private final Map<ZoneRuleSetType, McBiFunction<ZoneRuleSetType, ArenaZoneHandler, ZoneRuleSetInterface>>                     zoneRuleSetTypes      = new HashMap<>();
    
    /**
     * The creator func by component rule set type
     */
    private final Map<ComponentRuleSetType, McBiFunction<ComponentRuleSetType, ArenaComponentHandler, ComponentRuleSetInterface>> componentRuleSetTypes = new HashMap<>();
    
    /**
     * The creator func by sign rule set type
     */
    private final Map<SignRuleSetType, McBiFunction<SignRuleSetType, ArenaSignHandler, SignRuleSetInterface>>                     signRuleSetTypes      = new HashMap<>();
    
    /**
     * Components per plugin
     */
    private final Map<String, Set<ComponentTypeId>>                                                                               componentsPerPlugin   = new HashMap<>();
    
    /**
     * Component creator functions
     */
    private final Map<ComponentTypeId, McSupplier<ArenaComponentHandler>>                                         components            = new HashMap<>();
    
    /**
     * Zones per plugin
     */
    private final Map<String, Set<ZoneTypeId>>                                                                                    zonesPerPlugin        = new HashMap<>();
    
    /**
     * Zone creator functions
     */
    private final Map<ZoneTypeId, McSupplier<ArenaZoneHandler>>                                                   zones                 = new HashMap<>();
    
    /**
     * Signs per plugin
     */
    private final Map<String, Set<SignTypeId>>                                                                                    signsPerPlugin        = new HashMap<>();
    
    /**
     * Sign creator functions
     */
    private final Map<SignTypeId, McSupplier<ArenaSignHandler>>                                                   signs                 = new HashMap<>();
    
    // TODO Watch for disabled plugins
    
    /** arena name check pattern */
    private static final Pattern                                                                                                  ARENA_NAME_PATTERN    = Pattern.compile("[^\\d\\p{L}-]"); //$NON-NLS-1$
    
    /** plugin instance. */
    private static MinigamesPlugin                                                                                                INSTANCE;
    
    @Override
    public void onEnable()
    {
        INSTANCE = this;
        if (McLibInterface.instance().getApiVersion() != McLibInterface.APIVERSION_1_0_0)
        {
            throw new IllegalStateException("Cannot enable minigameslib. You installed an incompatible McLib-Version. " + McLibInterface.instance().getApiVersion()); //$NON-NLS-1$
        }
        
        EnumServiceInterface.instance().registerEnumClass(this, MglibConfig.class);
        EnumServiceInterface.instance().registerEnumClass(this, MglibMessages.class);
        EnumServiceInterface.instance().registerEnumClass(this, MglibPerms.class);
        EnumServiceInterface.instance().registerEnumClass(this, MglibObjectTypes.class);
        
        EnumServiceInterface.instance().registerEnumClass(this, BasicArenaRuleSets.class);
        EnumServiceInterface.instance().registerEnumClass(this, BasicComponentRuleSets.class);
        EnumServiceInterface.instance().registerEnumClass(this, BasicComponentTypes.class);
        EnumServiceInterface.instance().registerEnumClass(this, BasicSignRuleSets.class);
        EnumServiceInterface.instance().registerEnumClass(this, BasicSignTypes.class);
        EnumServiceInterface.instance().registerEnumClass(this, BasicZoneRuleSets.class);
        EnumServiceInterface.instance().registerEnumClass(this, BasicZoneTypes.class);
        
        Bukkit.getServicesManager().register(MinigamesLibInterface.class, this, this, ServicePriority.Highest);
        Bukkit.getServicesManager().register(TaskManager.class, new TaskManager(), this, ServicePriority.Highest);
        
        McLibInterface.instance().registerEvent(this, ArenaCreatedEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaCreateEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaPlayerJoinedEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaPlayerJoinedSpectatorsEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaPlayerJoinEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaPlayerJoinSpectatorsEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaPlayerLeftEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaPlayerLeftSpectatorsEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaDeletedEvent.class);
        McLibInterface.instance().registerEvent(this, ArenaDeleteEvent.class);
        
        this.registerRuleset(this, BasicArenaRuleSets.BasicMatch, BasicMatch::new);
        
        this.registerArenaComponent(this, BasicComponentTypes.Empty, EmptyComponent::new);
        this.registerArenaComponent(this, BasicComponentTypes.Generic, GenericComponent::new);
        this.registerArenaComponent(this, BasicComponentTypes.Spawn, SpawnComponent::new);
        this.registerArenaSign(this, BasicSignTypes.Empty, EmptySign::new);
        this.registerArenaSign(this, BasicSignTypes.Generic, GenericSign::new);
        this.registerArenaSign(this, BasicSignTypes.Join, JoinSign::new);
        this.registerArenaSign(this, BasicSignTypes.Leave, LeaveSign::new);
        this.registerArenaZone(this, BasicZoneTypes.Empty, EmptyZone::new);
        this.registerArenaZone(this, BasicZoneTypes.Generic, GenericZone::new);
        this.registerArenaZone(this, BasicZoneTypes.Battle, BattleZone::new);
        this.registerArenaZone(this, BasicZoneTypes.Join, JoinZone::new);
        this.registerArenaZone(this, BasicZoneTypes.Leave, LeaveZone::new);
        this.registerArenaZone(this, BasicZoneTypes.Lobby, LobbyZone::new);
        this.registerArenaZone(this, BasicZoneTypes.Main, MainZone::new);
        this.registerArenaZone(this, BasicZoneTypes.Spectator, SpectatorZone::new);
        
        try
        {
            ObjectServiceInterface.instance().register(MglibObjectTypes.Arena, ArenaImpl.class);
        }
        catch (McException ex)
        {
            this.getLogger().log(Level.SEVERE, "Problems registering object types", ex); //$NON-NLS-1$
        }
        
        final String[] arenas = MglibConfig.Arenas.getStringList();
        for (final String arena : arenas)
        {
            try
            {
                this.checkArenaName(arena);
                this.getLogger().log(Level.INFO, "Found arena " + arena); //$NON-NLS-1$
                final ArenaImpl arenaImpl = new ArenaImpl(new File(this.getDataFolder(), "arenas/" + arena + ".yml")); //$NON-NLS-1$//$NON-NLS-2$
                this.arenasPerName.put(arena, arenaImpl);
            }
            catch (McException ex)
            {
                this.getLogger().log(Level.WARNING, "Problems loading arena", ex); //$NON-NLS-1$
            }
        }
        this.getLogger().log(Level.INFO, "Enabled mglib. Loaded arenas: " + this.arenasPerName.size()); //$NON-NLS-1$
        
        new InitTask().runTaskLater(this, 10);
    }
    
    /**
     * Returns the minigame plugin instance
     * 
     * @return singleton instance
     */
    public static MinigamesPlugin instance()
    {
        return INSTANCE;
    }
    
    /**
     * Checks for valid arena name
     * 
     * @param arena
     * @throws McException
     *             thrown for invalid arena names.
     */
    private void checkArenaName(String arena) throws McException
    {
        if (ARENA_NAME_PATTERN.matcher(arena).find())
        {
            throw new McException(MglibCoreErrors.InvalidArenaName, arena);
        }
    }
    
    @Override
    public void onDisable()
    {
        EnumServiceInterface.instance().unregisterAllEnumerations(this);
    }
    
    @Override
    public int getApiVersion()
    {
        return APIVERSION_1_0_0;
    }
    
    @Override
    public boolean debug()
    {
        return MglibConfig.Debug.getBoolean();
    }
    
    @Override
    public LibState getState()
    {
        return this.state;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        switch (command.getName())
        {
            case "mg2": //$NON-NLS-1$
                final CommandImpl cmd = new CommandImpl(sender, command, label, args, "/mg2"); //$NON-NLS-1$
                try
                {
                    McLibInterface.instance().runInNewContext(null, cmd, cmd.getPlayer(), null, null, () -> {
                        this.mg2Command.handle(cmd);
                    });
                }
                catch (McException e)
                {
                    cmd.send(e.getErrorMessage(), e.getArgs());
                }
                break;
            default:
                return false;
        }
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        String lastArg = null;
        String[] newArgs = null;
        if (args.length > 0)
        {
            lastArg = args[args.length - 1].toLowerCase();
            newArgs = Arrays.copyOf(args, args.length - 1);
        }
        
        switch (command.getName())
        {
            case "mg2": //$NON-NLS-1$
                final CommandImpl cmd = new CommandImpl(sender, command, null, newArgs, "/im"); //$NON-NLS-1$
                final String last = lastArg;
                try
                {
                    
                    return McLibInterface.instance().calculateInNewContext(null, cmd, cmd.getPlayer(), null, null, () -> {
                        McLibInterface.instance().setContext(McPlayerInterface.class, cmd.getPlayer());
                        return this.mg2Command.onTabComplete(cmd, last);
                    });
                }
                catch (McException e)
                {
                    cmd.send(e.getErrorMessage(), e.getArgs());
                }
                break;
            default:
                break;
        }
        return Collections.emptyList();
    }
    
    @Override
    public void initMinigame(Plugin plugin, MinigameProvider provider) throws McException
    {
        if (this.state != LibState.Initializing)
        {
            throw new McException(MglibCoreErrors.LibInWrongState);
        }
        if (this.minigamesPerPlugin.containsKey(plugin.getName()))
        {
            throw new McException(MglibCoreErrors.PluginMinigameDuplicate, plugin.getName());
        }
        if (this.minigamesPerName.containsKey(provider.getName()))
        {
            throw new McException(MglibCoreErrors.MinigameAlreadyRegistered, provider.getName());
        }
        final MinigameImpl minigame = new MinigameImpl(plugin, provider);
        this.minigamesPerPlugin.put(plugin.getName(), minigame);
        this.minigamesPerName.put(minigame.getName(), minigame);
    }
    
    @Override
    public MinigameInterface getMinigame(String name)
    {
        return this.minigamesPerName.get(name);
    }
    
    @Override
    public MinigameInterface getMinigame(Plugin plugin)
    {
        return this.minigamesPerPlugin.get(plugin.getName());
    }
    
    @Override
    public int getMinigameCount()
    {
        return this.minigamesPerName.size();
    }
    
    @Override
    public int getMinigameCount(String prefix)
    {
        return (int) this.minigamesPerName.keySet().stream().filter(p -> p.toLowerCase().startsWith(prefix.toLowerCase())).count();
    }
    
    @Override
    public Collection<MinigameInterface> getMinigames(int index, int limit)
    {
        return this.minigamesPerName.values().stream().skip(index).limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public Collection<MinigameInterface> getMinigames(String prefix, int index, int limit)
    {
        return this.minigamesPerName.values().stream().filter(p -> p.getName().toLowerCase().startsWith(prefix.toLowerCase())).skip(index).limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public void initExtension(Plugin plugin, ExtensionProvider provider) throws McException
    {
        if (this.state != LibState.Initializing)
        {
            throw new McException(MglibCoreErrors.LibInWrongState);
        }
        if (this.extensionsPerPlugin.containsKey(plugin.getName()))
        {
            throw new McException(MglibCoreErrors.PluginExtensionDuplicate, plugin.getName());
        }
        if (this.extensionsPerName.containsKey(provider.getName()))
        {
            throw new McException(MglibCoreErrors.ExtensionAlreadyRegistered, provider.getName());
        }
        final ExtensionImpl extension = new ExtensionImpl(plugin, provider);
        this.extensionsPerPlugin.put(plugin.getName(), extension);
        this.extensionsPerName.put(extension.getName(), extension);
    }
    
    @Override
    public int getExtensionCount()
    {
        return this.extensionsPerName.size();
    }
    
    @Override
    public int getExtensionCount(String prefix)
    {
        return (int) this.extensionsPerName.keySet().stream().filter(p -> p.toLowerCase().startsWith(prefix.toLowerCase())).count();
    }
    
    @Override
    public Collection<ExtensionInterface> getExtensions(int index, int limit)
    {
        return this.extensionsPerName.values().stream().skip(index).limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public Collection<ExtensionInterface> getExtensions(String prefix, int index, int limit)
    {
        return this.extensionsPerName.values().stream().filter(p -> p.getName().toLowerCase().startsWith(prefix.toLowerCase())).skip(index).limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public ExtensionInterface getExtension(String name)
    {
        return this.extensionsPerName.get(name);
    }
    
    @Override
    public int getArenaCount()
    {
        return this.arenasPerName.size();
    }
    
    @Override
    public int getArenaCount(String prefix)
    {
        return (int) this.arenasPerName.keySet().stream().filter(p -> p.toLowerCase().startsWith(prefix.toLowerCase())).count();
    }
    
    @Override
    public int getArenaCount(Plugin plugin)
    {
        return (int) this.arenasPerName.values().stream().filter(p -> p.getPlugin() == plugin).count();
    }
    
    @Override
    public int getArenaCount(Plugin plugin, String prefix)
    {
        return (int) this.arenasPerName.values().stream().filter(p -> p.getPlugin() == plugin).filter(p -> p.getInternalName().toLowerCase().startsWith(prefix.toLowerCase())).count();
    }
    
    @Override
    public int getArenaCount(ArenaTypeInterface type)
    {
        return (int) this.arenasPerName.values().stream().filter(p -> p.getType() == type).count();
    }
    
    @Override
    public int getArenaCount(ArenaTypeInterface type, String prefix)
    {
        return (int) this.arenasPerName.values().stream().filter(p -> p.getType() == type).filter(p -> p.getInternalName().toLowerCase().startsWith(prefix.toLowerCase())).count();
    }
    
    @Override
    public Collection<ArenaInterface> getArenas(int index, int limit)
    {
        return this.arenasPerName.values().stream().skip(index).limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public Collection<ArenaInterface> getArenas(String prefix, int index, int limit)
    {
        return this.arenasPerName.values().stream().filter(p -> p.getInternalName().toLowerCase().startsWith(prefix.toLowerCase())).skip(index).limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public Collection<ArenaInterface> getArenas(Plugin plugin, int index, int limit)
    {
        return this.arenasPerName.values().stream().filter(p -> p.getPlugin() == plugin).skip(index).limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public Collection<ArenaInterface> getArenas(Plugin plugin, String prefix, int index, int limit)
    {
        return this.arenasPerName.values().stream().filter(p -> p.getPlugin() == plugin).filter(p -> p.getInternalName().toLowerCase().startsWith(prefix.toLowerCase())).skip(index).limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public Collection<ArenaInterface> getArenas(ArenaTypeInterface type, int index, int limit)
    {
        return this.arenasPerName.values().stream().filter(p -> p.getType() == type).skip(index).limit(limit).collect(Collectors.toList());
    }
    
    @Override
    public Collection<ArenaInterface> getArenas(ArenaTypeInterface type, String prefix, int index, int limit)
    {
        return this.arenasPerName.values().stream().filter(p -> p.getType() == type).filter(p -> p.getInternalName().toLowerCase().startsWith(prefix.toLowerCase())).skip(index).limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public ArenaInterface getArena(String name)
    {
        return this.arenasPerName.get(name);
    }
    
    @Override
    public ArenaInterface create(String name, ArenaTypeInterface type) throws McException
    {
        if (type == null)
        {
            throw new McException(CommonMessages.InternalError, "arena type must not be null"); //$NON-NLS-1$
        }
        if (this.arenasPerName.containsKey(name))
        {
            throw new McException(MglibCoreErrors.ArenaDuplicate, name);
        }
        
        final ArenaCreateEvent createEvent = new ArenaCreateEvent(name, type);
        Bukkit.getPluginManager().callEvent(createEvent);
        if (createEvent.isCancelled())
        {
            throw new McException(createEvent.getVetoReason(), createEvent.getVetoReasonArgs());
        }
        final ArenaImpl arena = new ArenaImpl(name, type, new File(this.getDataFolder(), "arenas/" + name + ".yml")); //$NON-NLS-1$ //$NON-NLS-2$
        arena.saveData();
        
        final Set<String> arenas = new HashSet<>(this.arenasPerName.keySet());
        arenas.add(name);
        MglibConfig.Arenas.setStringList(arenas.toArray(new String[arenas.size()]));
        MglibConfig.Arenas.saveConfig();
        
        // everything ok, now we can add the arena to our internal map.
        this.arenasPerName.put(name, arena);
        
        final ArenaCreatedEvent createdEvent = new ArenaCreatedEvent(arena);
        Bukkit.getPluginManager().callEvent(createdEvent);
        return arena;
    }
    
    @Override
    public ArenaPlayerInterface getPlayer(McPlayerInterface player)
    {
        ArenaPlayerImpl impl = player.getSessionStorage().get(ArenaPlayerImpl.class);
        if (impl == null)
        {
            ArenaPlayerPersistentData persistent = player.getPersistentStorage().get(ArenaPlayerPersistentData.class);
            if (persistent == null)
            {
                persistent = new ArenaPlayerPersistentData();
                player.getPersistentStorage().set(ArenaPlayerPersistentData.class, persistent);
            }
            impl = new ArenaPlayerImpl(player, persistent);
            player.getSessionStorage().set(ArenaPlayerImpl.class, impl);
        }
        return impl;
    }
    
    @Override
    public void registerRuleset(Plugin plugin, ArenaRuleSetType ruleset, McBiFunction<ArenaRuleSetType, ArenaInterface, ArenaRuleSetInterface> creator)
    {
        this.ruleSetsPerPlugin.computeIfAbsent(plugin.getName(), k -> new HashSet<>()).add(ruleset);
        this.arenaRuleSetTypes.put(ruleset, creator);
    }
    
    /**
     * Returns the create function for given rule set type
     * 
     * @param type
     * @return creator function
     */
    public McBiFunction<ArenaRuleSetType, ArenaInterface, ArenaRuleSetInterface> creator(ArenaRuleSetType type)
    {
        return this.arenaRuleSetTypes.get(type);
    }
    
    /**
     * Returns the create function for given rule set type
     * 
     * @param type
     * @return creator function
     */
    public McBiFunction<ComponentRuleSetType, ArenaComponentHandler, ComponentRuleSetInterface> creator(ComponentRuleSetType type)
    {
        return this.componentRuleSetTypes.get(type);
    }
    
    /**
     * Returns the create function for given rule set type
     * 
     * @param type
     * @return creator function
     */
    public McBiFunction<SignRuleSetType, ArenaSignHandler, SignRuleSetInterface> creator(SignRuleSetType type)
    {
        return this.signRuleSetTypes.get(type);
    }
    
    /**
     * Returns the create function for given rule set type
     * 
     * @param type
     * @return creator function
     */
    public McBiFunction<ZoneRuleSetType, ArenaZoneHandler, ZoneRuleSetInterface> creator(ZoneRuleSetType type)
    {
        return this.zoneRuleSetTypes.get(type);
    }
    
    /**
     * Returns the create function for given type
     * 
     * @param type
     * @return creator function
     */
    public McSupplier<ArenaComponentHandler> creator(ComponentTypeId type)
    {
        return this.components.get(type);
    }
    
    /**
     * Returns the create function for given type
     * 
     * @param type
     * @return creator function
     */
    public McSupplier<ArenaZoneHandler> creator(ZoneTypeId type)
    {
        return this.zones.get(type);
    }
    
    /**
     * Returns the create function for given type
     * 
     * @param type
     * @return creator function
     */
    public McSupplier<ArenaSignHandler> creator(SignTypeId type)
    {
        return this.signs.get(type);
    }
    
    @Override
    public void registerRuleset(Plugin plugin, ComponentRuleSetType ruleset, McBiFunction<ComponentRuleSetType, ArenaComponentHandler, ComponentRuleSetInterface> creator)
    {
        this.ruleSetsPerPlugin.computeIfAbsent(plugin.getName(), k -> new HashSet<>()).add(ruleset);
        this.componentRuleSetTypes.put(ruleset, creator);
    }
    
    @Override
    public void registerRuleset(Plugin plugin, SignRuleSetType ruleset, McBiFunction<SignRuleSetType, ArenaSignHandler, SignRuleSetInterface> creator)
    {
        this.ruleSetsPerPlugin.computeIfAbsent(plugin.getName(), k -> new HashSet<>()).add(ruleset);
        this.signRuleSetTypes.put(ruleset, creator);
    }
    
    @Override
    public void registerRuleset(Plugin plugin, ZoneRuleSetType ruleset, McBiFunction<ZoneRuleSetType, ArenaZoneHandler, ZoneRuleSetInterface> creator)
    {
        this.ruleSetsPerPlugin.computeIfAbsent(plugin.getName(), k -> new HashSet<>()).add(ruleset);
        this.zoneRuleSetTypes.put(ruleset, creator);
    }
    
    @Override
    public void registerArenaComponent(Plugin plugin, ComponentTypeId type, McSupplier<ArenaComponentHandler> creator)
    {
        this.componentsPerPlugin.computeIfAbsent(plugin.getName(), k -> new HashSet<>()).add(type);
        this.components.put(type, creator);
    }
    
    @Override
    public void registerArenaZone(Plugin plugin, ZoneTypeId type, McSupplier<ArenaZoneHandler> creator)
    {
        this.zonesPerPlugin.computeIfAbsent(plugin.getName(), k -> new HashSet<>()).add(type);
        this.zones.put(type, creator);
    }
    
    @Override
    public void registerArenaSign(Plugin plugin, SignTypeId type, McSupplier<ArenaSignHandler> creator)
    {
        this.signsPerPlugin.computeIfAbsent(plugin.getName(), k -> new HashSet<>()).add(type);
        this.signs.put(type, creator);
    }
    
}
