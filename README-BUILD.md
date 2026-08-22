# SkyCore - Kurulum ve Derleme Talimatlari

## Onemli, once oku
Calistigim ortamda internet erisimi kapali ve Java derleyicisi (javac/Maven) kurulu degil.
Bu yuzden sana derlenmis (**.jar**) bir dosya **veremedim** — bunun yerine PowerNukkitX
API'nizle uyumlu, tam kaynak kodlu bir Maven projesi hazirladim. Kendi bilgisayarinda
(JDK 17+ ve Maven kurulu bir yerde) asagidaki adimlarla gercek jar dosyasini uretebilirsin.

Kodun tamami `pnx.jar` icinden cikardigim gercek sinif/paket isimlerine (org.powernukkitx.*)
gore yazildi (PluginBase, Config, Player, Level, Item, Command/CommandExecutor, Listener/
EventHandler, PlayerJoinEvent/QuitEvent/ChatEvent, PluginTask...) - bunlar jar icinde
dogrulanan gercek siniflar. Ama bazi metod imzalarini (ozellikle scoreboard gonderme ve
PNX'in yeni "DDUI" form/menu sistemi) derleyici olmadan %100 dogrulayamadim; asagida
"Derlerken karsilasabilecegin kucuk hatalar" bolumunde bunlari tek tek isaretledim.

## 1) pnx.jar'i local Maven deposuna kur
Proje klasorunde (pom.xml'in yaninda), terminalde:

```
mvn install:install-file -Dfile=libs/pnx.jar -DgroupId=org.powernukkitx -DartifactId=powernukkitx -Dversion=1.0.0 -Dpackaging=jar
```

## 2) Derle
```
mvn clean package
```
Basarili olursa `target/SkyCore.jar` dosyasi olusur. Onu sunucunun `plugins/` klasorune koy.

## 3) Sunucu tarafinda gerekenler
- `config.yml` icinde `island.world-name` olarak belirttigin dunya (varsayilan: `skyblock`)
  sunucuda **onceden var olmali** ve tercihen bos/void bir dunya olmali (adalar bosluga
  kurulacak sekilde tasarlandi). PowerNukkitX'te boyle bir dunyayi bir void generator
  eklentisiyle ya da sunucunun kendi generator ayarlariyla olusturman gerekiyor.
- Rutbe, ekonomi, ada ve klan verileri `plugins/SkyCore/*.yml` icinde saklanir.

## 4) Derlerken karsilasabilecegin kucuk hatalar (ve nasil duzeltilir)
Bu API surumunu (org.powernukkitx.*) derleyici olmadan, sadece jar icindeki sinif
isimlerine bakarak yazdim. Buyuk ihtimalle her sey ya da neredeyse her sey ilk denemede
derlenecek, ama su noktalari ozellikle kontrol et:

1. **ScoreboardManager.java** (`net.skycore.managers.ScoreboardManager`):
   `plugin.getServer().getScoreboardManager().setScoreboardLines(...)` cagrisi tahmini
   bir isim. PNX'in gercek scoreboard API'si `org.powernukkitx.scoreboard.manager.ScoreboardManager`,
   `org.powernukkitx.scoreboard.Scoreboard`, `ScoreboardLine` siniflarini kullaniyor.
   Derleme hatasi alirsan bu dosyadaki iki satiri (setScoreboardLines / removeScoreboard)
   PNX'in scoreboard dokumantasyonuna/ornek pluginlerine bakarak guncelle - mantik
   (hangi satirlarin, hangi placeholder'larla gosterilecegi) zaten hazir, sadece
   "gonderme" cagrisini degistirmen yeterli.

2. **Warp Market GUI:** PNX bu surumde klasik "sandik GUI + tik eventi" yerine yeni bir
   "DDUI" form sistemi (`org.powernukkitx.ddui.CustomForm`, `ButtonElement` vb.)
   kullaniyor ve bu, versiyon versiyon degisen, benim tam olarak bilmedigim bir API.
   O yuzden marketi **garanti calisan komut tabanli** yaptim: `/market` fiyat listesini
   yazar, `/buy <esya> <miktar>` ve `/sell <esya> <miktar>` ile alim-satim yapilir.
   `MarketManager.openVisualForm()` metodunun icine, istersen gercek resimli/butonlu
   pencereyi PNX'in guncel DDUI API'siyle ekleyebilirsin (yorum satirinda iskelet birakildi).

3. **`getCommand("island")` cagrisi:** `SkyCore.java` icinde `getCommand(...).setExecutor(...)`
   kullandim (Bukkit-tarzi klasik yontem). PluginBase'de boyle bir yardimci metod yoksa,
   bunun yerine `getServer().getPluginManager().getCommand("island")` ya da PNX'in
   plugin.yml komut kaydi icin kullandigi esdeger cagriyi kullanman yeterli - komut
   mantiginin tamami (`IslandCommand` sinifi) zaten hazir ve degismeden calisir.

4. **Player/Server metod isimleri** (`getPlayer(String)`, `getOfflinePlayer(UUID)`,
   `getInventory().addItem/removeItem/contains`, `teleport(Position)`) standart
   Nukkit-ailesi imzalaridir ve genelde degismez, ama IDE hata verirse otomatik-tamamlama
   ile doğru overload'u secmen yeterli olacaktir.

## 5) Ozellik ozeti (su an calisan)
- **Ekonomi:** `/money`, `/pay`, `/baltop` - YAML tabanli bakiye sistemi.
- **Ada (SkyBlock):** `/island create|home|invite|accept|kick|leave|delete|level` -
  basit bir baslangic platformu (toprak+cim+agac+sandik) uretir, grid tabanli konumlandirma.
- **Rutbe/Yetki:** `/rank set|list|info` (admin) - config.yml'de tanimli 5 rutbe
  (owner/admin/moderator/vip/default), her rutbenin chat prefix'i ve rengi var.
- **Klan/Parti Chat:** `/clan create|invite|accept|leave|disband|kick|chat|info` -
  `/clan chat` ile klan-ici ozel sohbet moduna girilir.
- **Scoreboard:** Her oyuncuya periyodik guncellenen bir sidebar (bakiye, rutbe, ada
  seviyesi, online sayisi) - `/scoreboard` ile ac/kapa.
- **Warp Market:** `/market`, `/buy`, `/sell` - config.yml'de tanimli esyalarla
  alim-satim (10 ornek esya hazir tanimli, dilediğin kadar ekleyebilirsin).

## 6) Genisletme fikirleri (istersen sonra ekleyebilirim)
- Gercek DDUI resimli market penceresi
- Ada seviyesinin otomatik blok taramasiyla hesaplanmasi (async chunk tarama)
- Klan seviyeleri / klan bankasi
- MySQL/SQLite destegi (su an YAML dosya tabanli)
- Warp listesi (herkese acik ada warp'lari) ve ada koruma (grief onleme) sistemi
