all:
	sh ./compile

priv:
	rm -rf bin/*
	tar -zvcf cc.tar.gz . --exclude=*.tar.gz

pub:
	rm -rf bin/*
	tar -zvcf cc_pub.tar.gz src/ compile countfeatures evaluate evaluatebeam hashcodedist init parse parsebeam trainbeam

aux:
	rm -rf bin/*
	tar -zvcf cc_aux.tar.gz . --exclude=src --exclude=*.tar.gz

transfer:
	scp cc.tar.gz dc561@squacco.cl.cam.ac.uk:/local/filespace/dc561/cc.tar.gz

transferaux:
	scp cc_aux.tar.gz dc561@squacco.cl.cam.ac.uk:/local/filespace/dc561/cc_aux.tar.gz

clean:
	rm -rf bin/*
