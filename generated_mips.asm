# auto generated MIPS file.
# VaaDecaf, a Decaf compiler made with Java, ANTLR and the Vaadin framework


.text
.globl main
SimpleFn:
	addi $sp, $sp, -4				# Adjust stack pointer
	sw $s0, 0($sp)					# Save reg
	addi $sp, $sp, -4				# Adjust stack pointer
	sw $s1, 0($sp)					# Save reg
	lw $t1, x_SimpleFn				# ld data x_SimpleFn
	lw $t2, y_SimpleFn				# ld data y_SimpleFn
	mult $t1, $t2
	mflo $t0
	lw $t2, z_SimpleFn				# ld data z_SimpleFn
	mult $t0, $t2
	mflo $s0
	sw $s0, x_SimpleFn				# str data
	lw $s2, 0($sp)					# Restore reg
	addi $sp, $sp, 4				# Adjust stack pointer
	lw $s1, 0($sp)					# Restore reg
	addi $sp, $sp, 4				# Adjust stack pointer
	jr $ra							# Jump to addr stored in $ra


main:
	li $t0, 137
	move $a0, $t0
	sw $a0, z_SimpleFn
	jal SimpleFn
	move $s0, $v0


	# ---------- Exit ----------
	li $v0, 10
	syscall


# ---------- data section ----------
.data
z_SimpleFn:				.word 0
x_SimpleFn:				.word 0
y_SimpleFn:				.word 0
