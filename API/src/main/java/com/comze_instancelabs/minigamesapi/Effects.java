package com.comze_instancelabs.minigamesapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comze_instancelabs.minigamesapi.util.ParticleEffectNew;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Effects {

	public static void playBloodEffect(Player p) {
		p.getWorld().playEffect(p.getLocation().add(0D, 1D, 0D), Effect.STEP_SOUND, 152);
	}

	public static void playEffect(Arena a, Location l, String effectname) {
		for (String p_ : a.getAllPlayers()) {
			if (Validator.isPlayerOnline(p_)) {
				Player p = Bukkit.getPlayer(p_);
				ParticleEffectNew eff = ParticleEffectNew.valueOf(effectname);
				eff.setId(152);
				eff.animateReflected(p, l, 1F, 3);
			}
		}
	}

	public static void playFakeBed(Arena a, Player p) throws Exception {
		playFakeBed(a, p, p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
	}

	public static void playFakeBed(Arena a, Player p, int x, int y, int z) throws Exception {
		Method getHandle = Class.forName("org.bukkit.craftbukkit." + MinigamesAPI.getAPI().version + ".entity.CraftPlayer").getMethod("getHandle");
		Field playerConnection = Class.forName("net.minecraft.server." + MinigamesAPI.getAPI().version + ".EntityPlayer").getField("playerConnection");
		playerConnection.setAccessible(true);
		Method sendPacket = playerConnection.getType().getMethod("sendPacket", Class.forName("net.minecraft.server." + MinigamesAPI.getAPI().version + ".Packet"));

		Constructor packetPlayOutNamedEntityConstr = Class.forName("net.minecraft.server." + MinigamesAPI.getAPI().version + ".PacketPlayOutNamedEntitySpawn").getConstructor(Class.forName("net.minecraft.server." + MinigamesAPI.getAPI().version + ".EntityHuman"));
		Constructor packetPlayOutBedConstr = Class.forName("net.minecraft.server." + MinigamesAPI.getAPI().version + ".PacketPlayOutBed").getConstructor();

		Object packet = packetPlayOutNamedEntityConstr.newInstance(getHandle.invoke(p));
		setValue(packet, "a", -p.getEntityId());

		Object packet_ = packetPlayOutBedConstr.newInstance();
		setValue(packet_, "a", -p.getEntityId());
		setValue(packet_, "b", x);
		setValue(packet_, "c", y);
		setValue(packet_, "d", z);

		for (String p_ : a.getAllPlayers()) {
			Player p__ = Bukkit.getPlayer(p_);
			sendPacket.invoke(playerConnection.get(getHandle.invoke(p__)), packet);
			sendPacket.invoke(playerConnection.get(getHandle.invoke(p__)), packet_);
		}
	}

	private static void setValue(Object instance, String fieldName, Object value) throws Exception {
		Field field = instance.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(instance, value);
	}

}
