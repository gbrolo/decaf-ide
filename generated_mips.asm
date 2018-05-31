# auto generated MIPS file.
# VaaDecaf, a Decaf compiler made with Java, ANTLR and the Vaadin framework


.text
.globl main
main:
	lw $t1, var1_main				# ld data var1_main
	lw $t2, var2_main				# ld data var2_main
	slt $t0, $t1, $t2
	blez $t0, _L0
	lw $t2, var1_main				# ld data var1_main
	move $s0, $t2
	sw $s0, var3_main				# str data
	b _L1
	_L0:
	lw $t2, var2_main				# ld data var2_main
	move $s1, $t2
	sw $s1, var3_main				# str data
	_L1:
	lw $t2, var3_main				# ld data var3_main
	lw $t1, var3_main				# ld data var3_main
	mult $t2, $t1
	mflo $s2
	sw $s2, var3_main				# str data


	# ---------- Exit ----------
	li $v0, 10
	syscall


# ---------- data section ----------
.data
var1_main:				.word 0
var2_main:				.word 0
var3_main:				.word 0
