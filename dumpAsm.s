.text
.globl main
main:
		pushq   %rbp
		movq    %rsp, %rbp
		subq    $16, %rsp
		movl    $22, -4(%rbp)
		movl    $10, -8(%rbp)
		jmp    .L2
.L2:
		cmpl   $0, -4(%rbp)
		je    .L3
		cmpl   $0, -8(%rbp)
		jne   .L4
.L4:
		movl  -4(%rbp), %eax
		cmpl  -8(%rbp), %eax
		jle    .L5
		movl  -4(%rbp), %eax
		cltd
		idivl  -8(%rbp)
		movl   %edx, -4(%rbp)
		jmp    .L2
.L5:
		movl  -8(%rbp), %eax
		cltd
		idivl  -4(%rbp)
		movl   %edx, -8(%rbp)
.L3:
		movl  -4(%rbp), %edx
		movl  -8(%rbp), %eax
		addl  %edx, %eax
		movl  %edx, %esi
		movl    $.LC0, %edi
		movl    $0, %eax
		call    printf
		nop
		leave
		ret
.LC0:
		.string "%d"
