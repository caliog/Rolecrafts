package org.caliog.Rolecraft.Spells;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;
import org.caliog.Rolecraft.Manager;
import org.caliog.Rolecraft.Entities.EntityManager;
import org.caliog.Rolecraft.Entities.Player.RolecraftPlayer;
import org.caliog.Rolecraft.Spells.Mechanics.Spell;
import org.caliog.Rolecraft.XMechanics.Resource.FilePath;
import org.caliog.Rolecraft.XMechanics.Utils.IO.Debugger;
import org.caliog.Rolecraft.XMechanics.Utils.IO.Debugger.LogTitle;
import org.caliog.Rolecraft.XMechanics.Utils.VersionControll.Curses.VCDustOptions;

public class Curse extends Spell {

	public enum CurseType {
		// @formatter:off
		AQUA(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.AQUA, 1.5F)),
		BLACK(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.BLACK, 1.5F)),
		BLUE(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.BLUE, 1.5F)),
		FUCHSIA(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.FUCHSIA, 1.5F)),
		GRAY(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.GRAY, 1.5F)),
		GREEN(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.GREEN, 1.5F)),
		LIME(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.LIME, 1.5F)),
		MAROON(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.MAROON, 1.5F)),
		NAVY(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.NAVY, 1.5F)),
		OLIVE(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.OLIVE, 1.5F)),
		ORANGE(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.ORANGE, 1.5F)),
		PURPLE(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.PURPLE, 1.5F)),
		RED(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.RED, 1.5F)),
		SILVER(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.SILVER, 1.5F)),
		TEAL(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.TEAL, 1.5F)),
		WHITE(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.WHITE, 1.5F)),
		YELLOW(Particle.REDSTONE, 0F, VCDustOptions.getDustOptions(Color.YELLOW, 1.5F)),
		DOLPHIN(Particle.DOLPHIN, 1F),
		MAGIC(Particle.CRIT_MAGIC, 0.5F),
		SPELL(Particle.SPELL,0.7F),
		WITCH(Particle.CLOUD, 0.6F),
		FLAME(Particle.FLAME, 1F),
		HEART(Particle.HEART, 0.7F),
		SWEEP(Particle.SWEEP_ATTACK,0.8F),
		AURA(Particle.TOWN_AURA,0.6F);
		// @formatter:on
		public final Particle effect;
		public final Object data;
		public final float color; // speed

		CurseType(Particle e, float c) {
			this(e, c, null);
		}

		CurseType(Particle e, float c, Object d) {
			effect = e;
			color = c;
			data = d;
		}

		public void cast(Location loc, int a) {
			if (data == null)
				loc.getWorld().spawnParticle(this.effect, loc, 8 + a, 0.1F, 0.1F, 0.1F, this.color);
			else
				loc.getWorld().spawnParticle(this.effect, loc, 8 + a, 0.1F, 0.1F, 0.1F, this.color, this.data);
		}
	}

	public enum Direction {
		RAY, CONE, FULL, AUTO_TARGET, TWIRL;
	}

	private CurseType type;
	private Direction dir;

	public Curse(RolecraftPlayer player, String identifier) {
		super(player, identifier);
		String[] a = load();
		if (a == null) {
			String b[] = { CurseType.BLACK.name(), Direction.RAY.name() };
			a = b;
			Debugger.warning(LogTitle.SPELL, "Returned null while trying to load curse: ", identifier);
		}
		try {
			type = CurseType.valueOf(a[0]);
			dir = Direction.valueOf(a[1]);
		} catch (IllegalArgumentException e) {
			type = CurseType.BLACK;
			dir = Direction.RAY;
			e.printStackTrace();
		}
	}

	private String[] load() {
		File f = new File(FilePath.spells + getIdentifier() + ".yml");
		if (!f.exists())
			return null;
		YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
		ConfigurationSection section = config.getConfigurationSection("curse");
		String c = section.getString("color");
		String d = section.getString("direction");
		String[] a = { c, d };
		return a;
	}

	@Override
	public boolean execute() {
		if (!super.execute())
			return false;
		Manager.scheduleTask(new Runnable() {

			@Override
			public void run() {
				effects();
			}
		});

		return true;
	}

	private void effects() {
		final int distance = dir.equals(Direction.TWIRL) ? 40 : 20;
		Collection<Vector> vectorList = calculateVectors();
		final Location center = getPlayer().getPlayer().getEyeLocation();
		center.add(0, -0.3, 0);
		final int a = (int) (20 * ((float) getPower()) / getMaxPower());

		// entities
		Collection<Entity> entities = center.getWorld().getNearbyEntities(center, distance, distance / 4, distance);
		entities.remove(getPlayer().getPlayer());
		final Collection<Entity> temp = new ArrayList<Entity>();

		// Collect targets for auto target
		HashMap<Vector, Entity> targets = new HashMap<Vector, Entity>();
		if (dir.equals(Direction.AUTO_TARGET)) {
			int c = 0;
			for (Entity e : entities) {
				targets.put(e.getLocation().toVector().subtract(getPlayer().getPlayer().getLocation().toVector()), e);
				c++;
				if (c > (a / 2) + 1)
					break;
			}
			vectorList = targets.keySet();
			temp.addAll(targets.values());
		}

		for (final Vector v : vectorList) {
			// collect entities which are close to fireline
			if (!dir.equals(Direction.AUTO_TARGET)) {
				for (Entity entity : entities) {
					if (temp.contains(entity))
						continue;
					if (!entity.getType().equals(EntityType.VILLAGER) && (entity.getType().equals(EntityType.PLAYER)
							|| EntityManager.isRegistered(entity.getUniqueId()))) {
						Vector b = entity.getLocation().toVector()
								.subtract(getPlayer().getPlayer().getLocation().toVector());
						if (v.clone().crossProduct(b).lengthSquared() < 0.4F) {
							temp.add(entity);
						}
					}
				}
			}

			// effects & damage
			for (int i = 1; i < distance; i++) {
				Location loc = center.clone().add(nextVector(v, i, distance, targets.get(v)));
				Manager.scheduleTask(new Runnable() {

					@Override
					public void run() {
						type.cast(loc, a);
						for (Entity t : temp) {
							Vector r = loc.toVector().clone().setY(0);
							if (r.distanceSquared(loc.toVector().clone().setY(0)) < 2.2) {
								EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(getPlayer().getPlayer(),
										t, DamageCause.CUSTOM, getDamage());
								Bukkit.getPluginManager().callEvent(event);
							}
						}
					}
				}, Math.round(i / 3F));

			}
		}

	}

	private Vector nextVector(Vector v, int i, int distance, Entity target) {
		float p = (float) i / (float) distance;

		if (dir.equals(Direction.TWIRL)) {
			Vector v1 = new Vector(-Math.signum(v.getX()) * v.getY(), Math.signum(v.getX()) * v.getX(), 0);
			v1.normalize();
			v1.multiply(0.5F + p * 0.2F);
			Vector v2 = v1.clone().crossProduct(v).multiply(-1D);

			double angle = p * 6 * Math.PI;
			Vector w = v.clone().multiply(i / 2F);
			w.add(v1.clone().multiply(Math.sin(angle)));
			w.add(v2.clone().multiply(Math.cos(angle)));
			return w;
		} else if (dir.equals(Direction.AUTO_TARGET)) {
			if (target != null)
				return target.getLocation().toVector().clone()
						.subtract(getPlayer().getPlayer().getLocation().toVector()).normalize().multiply(i);
		}
		return v.clone().multiply(i);
	}

	private List<Vector> calculateVectors() {
		List<Vector> vectorList = new ArrayList<Vector>();
		double pitch = (getPlayer().getPlayer().getLocation().getPitch() + 90.0F) * (Math.PI / 180.0D);
		double yaw = (getPlayer().getPlayer().getLocation().getYaw() + 90.0F) * (Math.PI / 180.0D);
		double x = Math.sin(pitch) * Math.cos(yaw);
		double y = Math.sin(pitch) * Math.sin(yaw);
		double z = Math.cos(pitch);
		Vector look = new Vector(x, z, y);

		if (dir.equals(Direction.CONE) || dir.equals(Direction.RAY) || dir.equals(Direction.TWIRL))
			vectorList.add(look);

		final double angle = 3.141592653589793D / 8D;

		if (dir.equals(Direction.CONE)) {
			x = Math.sin(pitch) * Math.cos(yaw + angle);
			y = Math.sin(pitch) * Math.sin(yaw + angle);
			z = Math.cos(pitch);
			look = new Vector(x, z, y);
			vectorList.add(look);
			x = Math.sin(pitch) * Math.cos(yaw - angle);
			y = Math.sin(pitch) * Math.sin(yaw - angle);
			z = Math.cos(pitch);
			look = new Vector(x, z, y);
			vectorList.add(look);
			x = Math.sin(pitch + angle) * Math.cos(yaw);
			y = Math.sin(pitch + angle) * Math.sin(yaw);
			z = Math.cos(pitch + angle);
			look = new Vector(x, z, y);
			vectorList.add(look);
			x = Math.sin(pitch - angle) * Math.cos(yaw);
			y = Math.sin(pitch - angle) * Math.sin(yaw);
			z = Math.cos(pitch - angle);
			look = new Vector(x, z, y);
			vectorList.add(look);
		} else if (dir.equals(Direction.FULL)) {
			for (int i = 0; i < 8; i++) {
				x = Math.cos(i * Math.PI / 4D);
				y = Math.sin(i * Math.PI / 4D);
				look = new Vector(x, 0, y);
				vectorList.add(look);
			}
		}
		return vectorList;
	}

}
