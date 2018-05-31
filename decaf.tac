main:
	BeginFunc 12;
	b = 2;
	c = 2;
	d = 2;
	_t0 = c*d;
	a = b+_t0;
	PushParam d;
	LCall print;
	PopParams 1;
	_t1 = d;
	d = a*b;
	EndFunc;
