# auto generated MIPS file.
# VaaDecaf, a Decaf compiler made with Java, ANTLR and the Vaadin framework


.text
.globl main
main:
	lw $t1, b_main				# ld data b_main
	lw $t2, c_main				# ld data c_main
	add $t0, $t1, $t2
	lw $t2, d_main				# ld data d_main
	add $s0, $t0, $t2
	sw $s0, a_main				# str data


	lw $t2, a_main				# ld data a_main
	lw $t0, a_main				# ld data a_main
	mult $t2, $t0
	mflo $t1
	lw $t0, b_main				# ld data b_main
	lw $t2, b_main				# ld data b_main
	mult $t0, $t2
	mflo $t2
	add $s0, $t1, $t2
	sw $s0, b_main				# str data


	# ---------- Exit ----------
	li $v0, 10
	syscall


# ---------- data section ----------
.data
a_main:				.word 0
b_main:				.word 0
c_main:				.word 0
d_main:				.word 0
