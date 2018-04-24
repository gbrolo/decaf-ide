_fib:
	BeginFunc 44;
	_t0 = var1==0;
	Ifz _t0 Goto _L0;
	var2 = 1;
	Goto _L1;

	_L0:
	if = var1==1;

	_L1:
	var2 = var1==1;
	Ifz _t0 Goto _L2;
	var2 = 1;
	Goto _L3;

	_L2:

	_L3:
	var2 = var1-1;
	PushParam _t1;
	LCall _fib;
	PopParams 4;
	_t2 = var2-2;
	_t1 = var1;
	PushParam _t2;
	PushParam _t3;
	LCall _fib;
	PopParams 8;
	_t4 = var2;
	_t5 = var2;
	_t3 = var2;
	Return _t4;
	EndFunc;
main:
	BeginFunc 8;
	_t0 = a;
	PushParam _t0;
	LCall _fib;
	PopParams 4;
	EndFunc;
