main:
	BeginFunc 12;
	_t1 = y*y;
	_t0 = x*x;
	m2 = _t0+_t1;

	_L0:
	_t2 = 5<m2;
	Ifz _t2 Goto _L1;
	m2 = m2-x;
	Goto _L0:

	_L1:
	m2 = m2;
	EndFunc;
