-- word is a one word
-- attrs is  set of word attributes
--		verb (Indefinido,Participio,Gerundio,Other)
--		noun (Gender(m,f),Number(s,p),Pronoun(zaimek),Article(rodzajnik))
--		adjective ()
--		adverb ()
CREATE TABLE word (id INTEGER PRIMARY KEY,word VARCHAR(255),attrs varchar(10),UNIQUE(word));
