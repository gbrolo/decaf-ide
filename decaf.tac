_SimpleFn:
	BeginFunc 8;
	_t0 = x*y;
	x = _t0*z;
	EndFunc;
main:
	BeginFunc 8;
	_t0 = 137;
	PushParam _t0;
	LCall _SimpleFn;
	PopParams 4;
	EndFunc;
