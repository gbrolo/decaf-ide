# auto generated MIPS file.
# VaaDecaf, a Decaf compiler made with Java, ANTLR and the Vaadin framework


.text
.globl main
main:
	lw $t0, y_main				# ld data y_main
	lw $t2, y_main				# ld data y_main
	mult $t0, $t2
	mflo $t1
	lw $t2, x_main				# ld data x_main
	lw $t3, x_main				# ld data x_main
	mult $t2, $t3
	mflo $t0
	add $s0, $t0, $t1
	sw $s0, m2_main				# str data
	_L0:
	li $t1, 5
	lw $t0, m2_main				# ld data m2_main
	slt $t2, $t1, $t0
	bgtz $t2, _L1
	lw $t0, m2_main				# ld data m2_main
	lw $t1, x_main				# ld data x_main
	sub $s0, $t0, $t1
	sw $s0, m2_main				# str data
	b _L0
	_L1:


	# ---------- Exit ----------
	li $v0, 10
	syscall


# ---------- data section ----------
.data
x_main:				.word 0
y_main:				.word 0
m2_main:				.word 0
