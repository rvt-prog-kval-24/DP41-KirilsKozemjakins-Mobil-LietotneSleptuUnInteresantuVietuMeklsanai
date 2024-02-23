# ER-diagramma

![DP41 Kirils Kožemjakins ER-diagramma](https://media.discordapp.net/attachments/1210559886694547487/1210561845639782420/dp41-er-diagramma.jpg?ex=65eb0293&is=65d88d93&hm=be737f83a9d7a833e6d41b6e82928967b9a7dd730a454b36afc97debcf42bfa2&=&format=webp&width=922&height=662 "DP41 Kirils Kožemjakins ER-diagramma")

Sistēmas ER-modelis sastāv no 3 entītijām, kas nodrošina pamat informācijas uzglabāšanu un apstrādi. Tie ir: 
* Lietotājs –  šī entītija reprezentē sistēmas lietotājus. Katram lietotājam ir unikāls lietotājvārds un parole. Lietotājs var būt gan administrators, gan parasts lietotājs.
* Vieta: ši entītija reprezentē visus apmeklējamos objektus vai interesantas vietas, kuras lietotāji var meklēt un piedāvāt citiem lietotājiem. Katrai vietai ir savi unikāli identifikatori, kas ietver nosaukumu, aprakstu, adresi, kategoriju, koordinātas un citu saistītu informāciju.
* Atsauksmes – ši entītija satur informāciju par lietotāju veiktajām atsauksmēm par apmeklētajām vietām. Katra atsauksme saistās ar konkrētu vietu un lietotāju, un tai var būt vērtējums, komentārs un citas attiecīgas detaļas. 
Entītiju savstarpējās saistības ir šādas:
* Lietotājs ir saistīts ar Vietu, jo viens lietotājs var pievienot vairākas vietas.
* Lietotājs ir saistīts ar Atsauksmēm, jo viens lietotājs var uzrakstīt vairākas atsauksmes. 
