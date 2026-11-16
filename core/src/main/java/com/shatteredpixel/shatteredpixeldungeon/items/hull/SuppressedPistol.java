/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Shattered Hull
 * Copyright (C) 2026 Pablo Pochmann / Insane Goblin
 */

package com.shatteredpixel.shatteredpixeldungeon.items.hull;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

//Shattered Hull: pistola com supressor das Forças especiais.
//Fica na mochila, como o arco da Caçadora, e usa o mesmo carregador do fuzil.
//O tiro não faz barulho: ao contrário do fuzil, não atrai os inimigos em volta.
public class SuppressedPistol extends Weapon {

	public static final String AC_SHOOT  = "SHOOT";
	public static final String AC_RELOAD = "RELOAD";

	public int rounds = Magazine.CAPACITY;

	{
		image = ItemSpriteSheet.SUPPRESSED_PISTOL;

		defaultAction = AC_SHOOT;
		usesTargeting = true;

		unique = true;
		bones = false;
	}

	@Override
	public int min( int lvl ) {
		return 2 + lvl;
	}

	@Override
	public int max( int lvl ) {
		return 6 + Math.round(1.5f * lvl);
	}

	@Override
	public int STRReq( int lvl ) {
		return STRReq(1, lvl);
	}

	@Override
	public String defaultAction() {
		return AC_SHOOT;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_EQUIP);
		actions.add(AC_SHOOT);
		actions.add(AC_RELOAD);
		return actions;
	}

	@Override
	public String actionName( String action, Hero hero ) {
		if (action.equals(AC_SHOOT) || action.equals(AC_RELOAD)){
			return Messages.upperCase(Messages.get(this, "ac_" + action.toLowerCase()));
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute(hero, action);

		if (action.equals(AC_SHOOT)) {
			if (rounds <= 0){
				if (bestMagazine(hero) != null){
					reload(hero);
				} else {
					GLog.w(Messages.get(this, "empty"));
				}
				return;
			}
			curUser = hero;
			curItem = this;
			GameScene.selectCell( shooter );

		} else if (action.equals(AC_RELOAD)) {
			reload(hero);
		}
	}

	private Magazine bestMagazine( Hero hero ){
		Magazine best = null;
		for (Magazine m : hero.belongings.getAllItems(Magazine.class)){
			if (best == null || m.rounds > best.rounds) best = m;
		}
		return best;
	}

	public void reload( Hero hero ){
		if (rounds >= Magazine.CAPACITY){
			GLog.i(Messages.get(this, "already_full"));
			return;
		}
		Magazine best = bestMagazine(hero);
		if (best == null){
			GLog.w(Messages.get(this, "no_magazines"));
			return;
		}
		if (best.rounds <= rounds){
			GLog.w(Messages.get(this, "no_better_magazine"));
			return;
		}

		Magazine inserted = (Magazine) best.detach(hero.belongings.backpack);
		int old = rounds;
		rounds = inserted.rounds;

		if (old > 0){
			Magazine partial = new Magazine(old);
			if (!partial.collect(hero.belongings.backpack)){
				Dungeon.level.drop(partial, hero.pos).sprite.drop();
			}
			GLog.i(Messages.get(this, "reload_partial", old));
		} else {
			GLog.i(Messages.get(this, "reload"));
		}

		Sample.INSTANCE.play(Assets.Sounds.UNLOCK, 0.8f, 1.1f);
		hero.sprite.operate(hero.pos);
		updateQuickslot();
		hero.spendAndNext(1f);
	}

	@Override
	public String status() {
		return Integer.toString(rounds);
	}

	@Override
	public String info() {
		String info = desc();
		info += "\n\n" + Messages.get(this, "stats",
				augment.damageFactor(min()), augment.damageFactor(max()), STRReq());
		if (STRReq() > Dungeon.hero.STR()) {
			info += " " + Messages.get(Weapon.class, "too_heavy");
		}
		info += "\n\n" + Messages.get(this, "ammo", rounds, Magazine.CAPACITY);

		switch (augment) {
			case SPEED:
				info += "\n\n" + Messages.get(Weapon.class, "faster");
				break;
			case DAMAGE:
				info += "\n\n" + Messages.get(Weapon.class, "stronger");
				break;
			case NONE:
		}

		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.capitalize(Messages.get(Weapon.class, "enchanted", enchantment.name()));
			info += " " + enchantment.desc();
		}
		if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed");
		}
		return info;
	}

	@Override
	public int targetingPos( Hero user, int dst ) {
		return knockBullet().targetingPos(user, dst);
	}

	public Bullet knockBullet(){
		return new Bullet();
	}

	//tiro na nuca: mais dano contra quem não viu o atirador
	public static float bonusTiroNaNuca( Char attacker, Char defender ){
		if (attacker instanceof Hero && defender instanceof Mob
				&& ((Mob) defender).surprisedBy(attacker)){
			return 1f + 0.15f * ((Hero) attacker).pointsInTalent(Talent.SILENT_HEADSHOT);
		}
		return 1f;
	}

	public class Bullet extends MissileWeapon {

		{
			image = ItemSpriteSheet.RIFLE_BULLET;
			hitSound = Assets.Sounds.HIT_ARROW;
			setID = 0;
		}

		@Override
		public ArrayList<String> actions( Hero hero ) {
			return new ArrayList<>();
		}

		@Override
		public String defaultAction() {
			return null;
		}

		@Override
		public int defaultQuantity() {
			return 1;
		}

		@Override
		public int damageRoll( Char owner ) {
			int dmg = Hero.heroDamageIntRange(SuppressedPistol.this.min(), SuppressedPistol.this.max());
			dmg = augment.damageFactor(dmg);
			//disparo em movimento do Batedor, que antes valia só para armas de arremesso
			if (owner instanceof Hero && ((Hero) owner).subClass == HeroSubClass.FREERUNNER
					&& owner.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum.class) != null
					&& owner.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum.class).freerunning()){
				dmg = Math.round(dmg * (1f + 0.15f * ((Hero) owner).pointsInTalent(Talent.PROJECTILE_MOMENTUM)));
			}
			return dmg;
		}

		@Override
		public boolean hasEnchant( Class<? extends Weapon.Enchantment> type, Char owner ) {
			return SuppressedPistol.this.hasEnchant(type, owner);
		}

		@Override
		public int proc( Char attacker, Char defender, int damage ) {
			damage = Math.round(damage * bonusTiroNaNuca(attacker, defender));
			return SuppressedPistol.this.proc(attacker, defender, damage);
		}

		@Override
		public float delayFactor( Char user ) {
			return SuppressedPistol.this.delayFactor(user);
		}

		@Override
		public int STRReq( int lvl ) {
			return SuppressedPistol.this.STRReq();
		}

		@Override
		protected void onThrow( int cell ) {
			Char enemy = Actor.findChar( cell );
			if (enemy == null || enemy == curUser) {
				parent = null;
				Splash.at( cell, 0xFFFFD27A, 1 );
			} else if (!curUser.shoot( enemy, this )) {
				Splash.at( cell, 0xFFFFD27A, 1 );
			}
		}

		@Override
		public Item split( int amount ) {
			return null;
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play( Assets.Sounds.HIT_ARROW, 0.5f, Random.Float(0.6f, 0.75f) );
		}

		@Override
		public void cast( final Hero user, final int dst ) {
			rounds = Math.max(0, rounds - 1);
			updateQuickslot();
			super.cast(user, dst);
		}
	}

	private CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				knockBullet().cast(curUser, target);
			}
		}
		@Override
		public String prompt() {
			return Messages.get(SuppressedPistol.class, "prompt");
		}
	};

	private static final String ROUNDS = "rounds";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(ROUNDS, rounds);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(ROUNDS)) rounds = bundle.getInt(ROUNDS);
	}
}
