all:
	rm -f little_arith/*.class little_scheme/*.class
	javac -encoding utf-8 little_scheme/Main.java
	jar cfm little-scheme.jar Manifest little_arith/*.class little_scheme/*.class

clean:
	rm -f little_arith/*.class little_scheme/*.class
	rm -rf doc

distclean: clean
	rm -f little-scheme.jar *~ */*~

doc:
	javadoc -charset utf-8 -d doc little_arith little_scheme
