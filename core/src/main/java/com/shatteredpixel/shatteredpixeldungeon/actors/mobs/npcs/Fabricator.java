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


package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.Magazine;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.ServiceRifle;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FabricatorSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Bundle;

//Shattered Hull: fabricador da Vigília-7. É raro e cada um fabrica um único
//tipo de item, com energia para poucas fabricações. Custa Laztro.
public class Fabricator extends NPC {

	public enum Type {
		AMMO    (250, 3),
		MEDICAL (250, 2),
		ARMORY  (450, 1);

		public final int price;
		public final int charges;

		Type( int price, int charges ){
			this.price = price;
			this.charges = charges;
		}
	}

	//um a cada 2 ou 3 setores, sem cair em setor de chefe (5, 10, 15, 20, 25).
	//No setor 1 o preço fica acima do que o jogador consegue juntar até ali.
	public static Type typeForDepth( int depth ){
		switch (depth){
			case 1: case 9: case 17: case 24:
				return Type.AMMO;
			case 4: case 12: case 19:
				return Type.MEDICAL;
			case 7: case 14: case 22:
				return Type.ARMORY;
			default:
				return null;
		}
	}

	{
		spriteClass = FabricatorSprite.class;

		properties.add(Property.IMMOVABLE);
		properties.add(Property.INORGANIC);
	}

	public Type type = Type.AMMO;
	public int charges = Type.AMMO.charges;

	public Fabricator setType( Type type ){
		this.type = type;
		this.charges = type.charges;
		return this;
	}

	@Override
	public Notes.Landmark landmark() {
		return Notes.Landmark.SHOP;
	}

	@Override
	protected boolean act() {
		if (Dungeon.level.heroFOV[pos] && Dungeon.hero != null){
			Notes.add(landmark());
		}
		spend( TICK );
		return super.act();
	}

	@Override
	public void damage( int dmg, Object src ) {
		//blindado
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean isInvulnerable( Class effect ) {
		return true;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public String name() {
		return Messages.get(this, "name_" + type.name().toLowerCase());
	}

	@Override
	public String description() {
		return Messages.get(this, "desc") + "\n\n" + Messages.get(this, "desc_" + type.name().toLowerCase());
	}

	private Item makeProduct(){
		switch (type){
			case AMMO: default:
				if (Dungeon.hero.belongings.getItem(ServiceRifle.class) != null){
					return new Magazine();
				}
				return Generator.randomMissile();
			case MEDICAL:
				return new PotionOfHealing();
			case ARMORY:
				Armor armor = Generator.randomArmor();
				armor.cursed = false;
				if (armor.hasCurseGlyph()) armor.inscribe(null);
				if (armor.level() < 0) armor.level(0);
				return armor;
		}
	}

	private String productName(){
		switch (type){
			case AMMO: default:
				if (Dungeon.hero.belongings.getItem(ServiceRifle.class) != null){
					return new Magazine().name();
				}
				return Messages.get(this, "product_missile");
			case MEDICAL:
				return Messages.get(this, "product_medical");
			case ARMORY:
				return Messages.get(this, "product_armor");
		}
	}

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero){
			return true;
		}

		final String product = productName();
		String msg = Messages.get(this, "panel", Dungeon.gold, charges);
		if (charges <= 0){
			msg += "\n\n" + Messages.get(this, "depleted");
		} else if (Dungeon.gold < type.price){
			msg += "\n\n" + Messages.get(this, "not_enough", type.price - Dungeon.gold);
		}

		final String panel = msg;
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				showPanel(product, panel);
			}
		});
		return true;
	}

	private void showPanel( String product, String msg ){
		GameScene.show(new WndOptions(sprite(), Messages.titleCase(name()), msg,
				Messages.get(this, "craft", product, type.price),
				Messages.get(this, "dismantle"),
				Messages.get(this, "leave")){
			@Override
			protected boolean enabled( int index ) {
				if (index == 0){
					return charges > 0 && Dungeon.gold >= type.price;
				}
				return true;
			}

			@Override
			protected void onSelect( int index ) {
				if (index == 0) craft();
				else if (index == 1) chooseDismantle();
			}
		});
	}

	//Shattered Hull: desmontar converte um item da mochila em Laztro.
	//Funciona mesmo com o núcleo sem energia, porque reciclar não gasta carga.
	public static boolean canDismantle( Item item ){
		if (item instanceof ServiceRifle) return false;
		return Shopkeeper.canSell(item);
	}

	private void chooseDismantle(){
		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get(Fabricator.class, "dismantle_prompt");
			}

			@Override
			public boolean itemSelectable( Item item ) {
				return canDismantle(item);
			}

			@Override
			public void onSelect( Item item ) {
				if (item != null && canDismantle(item)) confirmDismantle(item);
			}
		});
	}

	private void confirmDismantle( final Item item ){
		final int value = item.value();
		GameScene.show(new WndOptions(new ItemSprite(item),
				Messages.titleCase(item.name()),
				Messages.get(Fabricator.class, "dismantle_confirm", item.name(), value),
				Messages.get(Fabricator.class, "dismantle_yes"),
				Messages.get(Fabricator.class, "dismantle_no")){
			@Override
			protected void onSelect( int index ) {
				if (index == 0) dismantle(item, value);
			}
		});
	}

	private void dismantle( Item item, int value ){
		String name = item.name();
		WndTradeItem.sell(item);
		Sample.INSTANCE.play( Assets.Sounds.EVOKE, 1f, 1.2f );
		if (sprite != null) sprite.emitter().burst( EnergyParticle.FACTORY, 6 );
		GLog.p( Messages.get(Fabricator.class, "dismantled", name, value) );
	}

	private void craft(){
		if (charges <= 0 || Dungeon.gold < type.price) return;

		Dungeon.gold -= type.price;
		Catalog.countUses(Gold.class, type.price);
		charges--;

		Item item = makeProduct();
		item.identify();
		if (!item.doPickUp( Dungeon.hero )) {
			Dungeon.level.drop( item, Dungeon.hero.pos ).sprite.drop();
		}

		Sample.INSTANCE.play( Assets.Sounds.EVOKE, 1f, 0.8f );
		if (sprite != null) sprite.emitter().burst( EnergyParticle.FACTORY, 8 );
		GLog.p( Messages.get(this, "crafted", item.name()) );
		if (charges <= 0){
			GLog.i( Messages.get(this, "now_depleted") );
		}
	}

	private static final String TYPE    = "fab_type";
	private static final String CHARGES = "fab_charges";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(TYPE, type);
		bundle.put(CHARGES, charges);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(TYPE)) type = bundle.getEnum(TYPE, Type.class);
		if (bundle.contains(CHARGES)) charges = bundle.getInt(CHARGES);
	}
}
