package org.caliog.Rolecraft.XMechanics.npclib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.caliog.Rolecraft.Manager;
import org.caliog.Rolecraft.XMechanics.Debug.Debugger;
import org.caliog.Rolecraft.XMechanics.Reflection.Reflect;
import org.caliog.Rolecraft.XMechanics.npclib.NMS.BWorld;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public abstract class NMSUtil {

	public static NMSUtil util = null;

	public abstract void setYaw(Entity entity, float yaw);

	public void pathStep(Moveable a) {
		try {
			Class<?> entityClass = Reflect.getNMSClass("Entity");
			Field yaw = entityClass.getField("yaw");
			Field pitch = entityClass.getField("pitch");
			Method setPositionRotation = entityClass.getMethod("setPositionRotation", double.class, double.class,
					double.class, float.class, float.class);

			float angle = yaw.getFloat(getHandle(a.getBukkitEntity()));
			float look = pitch.getFloat(getHandle(a.getBukkitEntity()));

			if (a.pathIterator.hasNext()) {
				Node n = (Node) a.pathIterator.next();
				if (n.b.getWorld() != a.getBukkitEntity().getWorld()) {
					a.getBukkitEntity().teleport(n.b.getLocation());
				} else {
					if ((a.last == null) || (a.runningPath.checkPath(n, a.last, true))) {
						if (a.last != null && !a.last.b.equals(n.b)) {
							angle = (float) Math
									.toDegrees(Math.atan2(a.last.b.getX() - n.b.getX(), n.b.getZ() - a.last.b.getZ()));
							look = (float) (Math.toDegrees(Math.asin(a.last.b.getY() - n.b.getY())) / 2.0D);
						}
						setPositionRotation.invoke(getHandle(a.getBukkitEntity()), n.b.getX() + 0.5D, n.b.getY(),
								n.b.getZ() + 0.5D, angle, look);
						setYaw(a.getBukkitEntity(), angle);
					} else {
						a.onFail.run();
					}
				}
				a.last = n;
			} else {
				// getHandle(a.getBukkitEntity()).setPositionRotation(a.runningPath.getEnd().getX(),
				// a.runningPath.getEnd().getY(),
				// a.runningPath.getEnd().getZ(), a.runningPath.getEnd().getYaw(),
				// a.runningPath.getEnd().getPitch());
				// setYaw(a.getBukkitEntity(), a.runningPath.getEnd().getYaw());
				Bukkit.getServer().getScheduler().cancelTask(a.taskid);
				a.taskid = 0;
			}

		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	public abstract NPCManager getnpcManager();

	public abstract Object getPlayerHandle(Player player);

	public abstract Object getHandle(Entity entity);

	public abstract Entity createNPCEntity(NPCManager manager, BWorld world, String name);

	public static void initUtil() {
		String version = Manager.getServerVersion();
		try {
			Class<?> raw = Class.forName("org.caliog.Rolecraft.XMechanics.npclib." + version + ".Util");
			Class<? extends NMSUtil> util = raw.asSubclass(NMSUtil.class);
			Constructor<? extends NMSUtil> constructor = util.getConstructor();
			NMSUtil.util = (NMSUtil) constructor.newInstance();
		} catch (ClassNotFoundException ex) {
			Debugger.exception("NMSUtil threw ClassNotFoundException (unsupported bukkit version).");
			Manager.plugin.getLogger().log(Level.WARNING, "Unsupported bukkit version! (" + version + ")");
		} catch (Exception e) {
			Debugger.exception("NMSUtil threw exception:", e.getMessage());
			e.printStackTrace();
		}

	}

	public static NMSUtil getUtil() {
		if (util == null)
			initUtil();
		return util;
	}

	public static NPCManager getNPCManager() {
		NMSUtil util = getUtil();
		if (util != null)
			return util.getnpcManager();
		return null;
	}

	public static void sendPacketsTo(Iterable<? extends Player> recipients, Object... packets) {
		try {
			Class<?> craftPlayerClass = Reflect.getCraftbukkitClass("entity.CraftPlayer");
			Class<?> entityPlayerClass = Reflect.getNMSClass("EntityPlayer");
			Class<?> playerConnectionClass = Reflect.getNMSClass("PlayerConnection");
			Class<?> packetClass = Reflect.getNMSClass("Packet");
			Iterable<Object> nmsRecipients = Iterables.transform(recipients, new Function<Player, Object>() {

				@Override
				public Object apply(Player a) {
					try {
						return craftPlayerClass.getMethod("getHandle").invoke(craftPlayerClass.cast(a));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
						return null;
					}
				}
			});
			for (Object recipient : nmsRecipients) {
				if (recipient != null) {
					for (Object packet : packets) {
						if (packet != null) {
							Object playerConnection = entityPlayerClass.getField("playerConnection").get(recipient);
							playerConnectionClass.getMethod("sendPacket", packetClass).invoke(playerConnection, packet);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendPacketsTo(Player player, Object... packet) {
		ArrayList<Player> it = new ArrayList<Player>();
		it.add(player);
		sendPacketsTo(it, packet);
	}

}
