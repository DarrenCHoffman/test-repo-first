# Space Shooter – Build & Run
SRCDIR  = src
OUTDIR  = out
SOURCES = $(wildcard $(SRCDIR)/spaceshooter/*.java)
MAIN    = spaceshooter.SpaceShooter

.PHONY: all run clean

all:
	mkdir -p $(OUTDIR)
	javac -d $(OUTDIR) $(SOURCES)

run: all
	java -cp $(OUTDIR) $(MAIN)

clean:
	rm -rf $(OUTDIR)
