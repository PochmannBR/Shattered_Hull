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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ArcaneBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.FrostBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.ShrapnelBomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.InjetorSupressor;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.Renegado;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.Sintetico;
import com.shatteredpixel.shatteredpixeldungeon.items.hull.SuppressedPistol;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfStamina;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.IncendiaryDart;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ParalyticDart;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.utils.Reflection;
import java.util.ArrayList;

//Shattered Hull: fabricador da Vigília-7. É raro e cada um fabrica uma linha
//de itens, com energia para poucas fabricações. Custa Laztro. Desde a 0.9.3,
//o jogador escolhe o item num catálogo que cresce com a região.
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
		return Notes.Landmark.FABRICATOR;
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

	//Shattered Hull 0.9.3: cada fabricador oferece o catálogo da região onde está
	//e das anteriores. O jogador escolhe o item, e cada um tem o próprio preço.
	public static class Oferta {
		public final String id;
		public final int preco;
		public Oferta( String id, int preco ){
			this.id = id;
			this.preco = preco;
		}
	}

	public static final int OFERTAS_POR_PAGINA = 6;

	public static final int PRECO_REVESTIMENTO    = 650;
	public static final int PRECO_REVESTIMENTO_P1 = 900;
	public static final int PRECO_REVESTIMENTO_P2 = 1200;

	public static int regiao(){
		return Math.max(1, Math.min(5, (Dungeon.depth - 1) / 5 + 1));
	}

	private static boolean temArmaDeFogo(){
		return Dungeon.hero.belongings.getItem(ServiceRifle.class) != null
				|| Dungeon.hero.belongings.getItem(SuppressedPistol.class) != null;
	}

	public ArrayList<Oferta> catalogo(){
		ArrayList<Oferta> l = new ArrayList<>();
		int r = regiao();
		boolean sintetico = Sintetico.e(Dungeon.hero);
		boolean renegado = Renegado.e(Dungeon.hero);
		switch (type){
			case AMMO: default:
				if (temArmaDeFogo()) l.add(new Oferta("carregador", 250));
				l.add(new Oferta("arremesso", 200));
				l.add(new Oferta("dardos", 150));
				if (r >= 2){
					l.add(new Oferta("bomba", 300));
					l.add(new Oferta("dardos_incendiarios", 250));
				}
				if (r >= 3){
					l.add(new Oferta("bomba_gelo", 350));
					l.add(new Oferta("bomba_estilhacos", 400));
				}
				if (r >= 4){
					l.add(new Oferta("dardos_paraliticos", 350));
					l.add(new Oferta("bomba_pulso", 400));
				}
				if (r >= 5 && temArmaDeFogo()){
					l.add(new Oferta("carregadores", 450));
				}
				break;
			case MEDICAL:
				if (!sintetico) l.add(new Oferta("regeneracao", 250));
				if (renegado) l.add(new Oferta("supressor", 250));
				l.add(new Oferta("biomassa", 150));
				if (!sintetico) l.add(new Oferta("racao", 150));
				if (r >= 2){
					l.add(new Oferta("antitoxina", 200));
					l.add(new Oferta("adrenalina", 250));
				}
				if (r >= 3){
					l.add(new Oferta("blindagem", 350));
				}
				if (r >= 4){
					l.add(new Oferta("vigor", 300));
					l.add(new Oferta("campo", 450));
				}
				break;
			case ARMORY:
				l.add(new Oferta("armadura", 450));
				l.add(new Oferta("arma", 450));
				if (r >= 3){
					l.add(new Oferta("revestimento", PRECO_REVESTIMENTO));
				}
				if (r >= 5){
					l.add(new Oferta("arma_modificada", 700));
				}
				break;
		}
		return l;
	}

	//Shattered Hull: logística do Intendente, 10% de desconto por ponto
	public static int desconto( int preco ){
		if (Dungeon.hero != null){
			preco = Math.round(preco * (1f - 0.1f * Dungeon.hero.pointsInTalent(Talent.LOGISTICA)));
		}
		return preco;
	}

	public static String nomeOferta( Oferta o ){
		return Messages.get(Fabricator.class, "oferta_" + o.id);
	}

	private static String rotulo( Oferta o ){
		String chave = o.id.equals("revestimento") ? "oferta_desde" : "oferta";
		return Messages.capitalize(Messages.get(Fabricator.class, chave, nomeOferta(o), desconto(o.preco)));
	}

	public static Item produzir( String id ){
		switch (id){
			case "carregador": return new Magazine();
			case "carregadores": return new Magazine().quantity(2);
			case "arremesso": return arremessoLimpo();
			case "dardos": return new Dart().quantity(3);
			case "dardos_incendiarios": return new IncendiaryDart().quantity(3);
			case "dardos_paraliticos": return new ParalyticDart().quantity(3);
			case "bomba": return new Bomb();
			case "bomba_gelo": return new FrostBomb();
			case "bomba_estilhacos": return new ShrapnelBomb();
			case "bomba_pulso": return new ArcaneBomb();
			case "regeneracao": return new PotionOfHealing();
			case "supressor": return new InjetorSupressor();
			case "biomassa": return new Dewdrop().quantity(10);
			case "racao": return new Food();
			case "antitoxina": return new PotionOfPurity();
			case "adrenalina": return new PotionOfHaste();
			case "blindagem": return new PotionOfShielding();
			case "vigor": return new PotionOfStamina();
			case "campo": return new ElixirOfArcaneArmor();
			case "armadura": return armaduraLimpa();
			case "arma": return armaLimpa();
			case "arma_modificada": {
				MeleeWeapon w = armaLimpa();
				w.enchant(Weapon.Enchantment.random());
				return w;
			}
			default: return null;
		}
	}

	private static Item arremessoLimpo(){
		Item it = Generator.randomMissile();
		it.cursed = false;
		if (it instanceof Weapon && ((Weapon) it).hasCurseEnchant()) ((Weapon) it).enchant(null);
		if (it.level() < 0) it.level(0);
		return it;
	}

	private static Armor armaduraLimpa(){
		Armor armor = Generator.randomArmor();
		armor.cursed = false;
		if (armor.hasCurseGlyph()) armor.inscribe(null);
		if (armor.level() < 0) armor.level(0);
		return armor;
	}

	private static MeleeWeapon armaLimpa(){
		MeleeWeapon w = Generator.randomWeapon();
		w.cursed = false;
		if (w.hasCurseEnchant()) w.enchant(null);
		if (w.level() < 0) w.level(0);
		return w;
	}

	//armadura sorteada do setor com o revestimento escolhido e o reforço pago
	public static Armor armaduraRevestida( Class<? extends Armor.Glyph> glyph, int reforco ){
		Armor armor = armaduraLimpa();
		armor.level(reforco);
		armor.inscribe((Armor.Glyph) Reflection.newInstance(glyph));
		return armor;
	}

	@Override
	public boolean interact( Char c ) {
		if (c != Dungeon.hero){
			return true;
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				showPanel(0);
			}
		});
		return true;
	}

	private void showPanel( final int pagina ){
		final ArrayList<Oferta> todas = catalogo();
		final int inicio = pagina * OFERTAS_POR_PAGINA;
		final int fimPag = Math.min(todas.size(), inicio + OFERTAS_POR_PAGINA);
		final boolean temMais = fimPag < todas.size() || pagina > 0;

		final ArrayList<Oferta> nestaPagina = new ArrayList<>(todas.subList(inicio, fimPag));
		ArrayList<String> botoes = new ArrayList<>();
		for (Oferta o : nestaPagina) botoes.add(rotulo(o));
		if (temMais) botoes.add(Messages.get(this, fimPag < todas.size() ? "mais" : "inicio"));
		botoes.add(Messages.get(this, "dismantle"));
		botoes.add(Messages.get(this, "leave"));

		String msg = Messages.get(this, "panel", Dungeon.gold, charges);
		if (charges <= 0){
			msg += "\n\n" + Messages.get(this, "depleted");
		}

		final int nOfertas = nestaPagina.size();
		GameScene.show(new WndOptions(sprite(), Messages.titleCase(name()), msg,
				botoes.toArray(new String[0])){
			@Override
			protected boolean enabled( int index ) {
				if (index < nOfertas){
					return charges > 0 && Dungeon.gold >= desconto(nestaPagina.get(index).preco);
				}
				return true;
			}

			@Override
			protected void onSelect( int index ) {
				if (index < nOfertas){
					Oferta o = nestaPagina.get(index);
					if (o.id.equals("revestimento")) escolherGrupo();
					else craft(o.id, o.preco);
					return;
				}
				int resto = index - nOfertas;
				if (temMais){
					if (resto == 0){
						showPanel(fimPag < todas.size() ? pagina + 1 : 0);
						return;
					}
					resto--;
				}
				if (resto == 0) chooseDismantle();
			}
		});
	}

	private void escolherGrupo(){
		GameScene.show(new WndOptions(sprite(), Messages.titleCase(name()),
				Messages.get(this, "escolha_grupo"),
				Messages.get(this, "grupo_comum"),
				Messages.get(this, "grupo_incomum"),
				Messages.get(this, "grupo_raro"),
				Messages.get(this, "voltar")){
			@Override
			protected void onSelect( int index ) {
				switch (index){
					case 0: escolherRevestimento(Armor.Glyph.common); break;
					case 1: escolherRevestimento(Armor.Glyph.uncommon); break;
					case 2: escolherRevestimento(Armor.Glyph.rare); break;
					default: showPanel(0);
				}
			}
		});
	}

	public static String nomeRevestimento( Class<?> glyph ){
		return ((Armor.Glyph) Reflection.newInstance(glyph)).name(Messages.get(Fabricator.class, "armadura"));
	}

	private void escolherRevestimento( final Class<?>[] grupo ){
		String[] botoes = new String[grupo.length + 1];
		for (int i = 0; i < grupo.length; i++){
			botoes[i] = Messages.capitalize(nomeRevestimento(grupo[i]));
		}
		botoes[grupo.length] = Messages.get(this, "voltar");
		GameScene.show(new WndOptions(sprite(), Messages.titleCase(name()),
				Messages.get(this, "escolha_revestimento"), botoes){
			@Override
			protected boolean hasInfo( int index ) {
				return index < grupo.length;
			}

			@Override
			protected void onInfo( int index ) {
				Armor.Glyph g = (Armor.Glyph) Reflection.newInstance(grupo[index]);
				GameScene.show(new WndTitledMessage(sprite(),
						Messages.titleCase(nomeRevestimento(grupo[index])), g.desc()));
			}

			@Override
			protected void onSelect( int index ) {
				if (index < grupo.length) escolherReforco((Class<? extends Armor.Glyph>) grupo[index]);
				else escolherGrupo();
			}
		});
	}

	public static int[] precosReforco(){
		if (regiao() >= 5){
			return new int[]{PRECO_REVESTIMENTO, PRECO_REVESTIMENTO_P1, PRECO_REVESTIMENTO_P2};
		}
		return new int[]{PRECO_REVESTIMENTO, PRECO_REVESTIMENTO_P1};
	}

	private void escolherReforco( final Class<? extends Armor.Glyph> glyph ){
		final int[] precos = precosReforco();
		String[] botoes = new String[precos.length + 1];
		for (int i = 0; i < precos.length; i++){
			botoes[i] = Messages.get(this, "reforco_" + i, desconto(precos[i]));
		}
		botoes[precos.length] = Messages.get(this, "voltar");
		GameScene.show(new WndOptions(sprite(), Messages.titleCase(name()),
				Messages.get(this, "escolha_reforco", nomeRevestimento(glyph)), botoes){
			@Override
			protected boolean enabled( int index ) {
				if (index < precos.length){
					return charges > 0 && Dungeon.gold >= desconto(precos[index]);
				}
				return true;
			}

			@Override
			protected void onSelect( int index ) {
				if (index < precos.length){
					if (charges <= 0 || Dungeon.gold < desconto(precos[index])) return;
					entregar(armaduraRevestida(glyph, index), desconto(precos[index]));
				} else {
					escolherRevestimento(grupoDe(glyph));
				}
			}
		});
	}

	private static Class<?>[] grupoDe( Class<?> glyph ){
		for (Class<?> c : Armor.Glyph.uncommon) if (c == glyph) return Armor.Glyph.uncommon;
		for (Class<?> c : Armor.Glyph.rare) if (c == glyph) return Armor.Glyph.rare;
		return Armor.Glyph.common;
	}

	//Shattered Hull: desmontar converte um item da mochila em Laztro.
	//Funciona mesmo com o núcleo sem energia, porque reciclar não gasta carga.
	public static boolean canDismantle( Item item ){
		if (item instanceof ServiceRifle) return false;
		if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.hull.SuppressedPistol) return false;
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

	private void craft( String id, int precoBase ){
		int preco = desconto(precoBase);
		if (charges <= 0 || Dungeon.gold < preco) return;
		Item item = produzir(id);
		if (item == null) return;
		entregar(item, preco);
	}

	private void entregar( Item item, int preco ){
		Dungeon.gold -= preco;
		Catalog.countUses(Gold.class, preco);
		charges--;

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
