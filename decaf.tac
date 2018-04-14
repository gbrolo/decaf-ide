main:
	BeginFunc N;

	_L0:
	_t0 = x<y;
	Ifz _t0 Goto _L1;
	x = x*2;
	Goto _L0:

	_L1:
