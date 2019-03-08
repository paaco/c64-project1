;
; scroll idea:
; - dubbel buffer
; - maximaal 4 rijen en 4 kolommen (32 scrollbits) per keer (offset is dan 4x40+4=164 tot 204)

SRC=$0400
DST=$0400

* = $0801

!basic 2018, start

start:
		ldx #40		; source offset
		ldy #0		; dest offset
loop:
!for ROW = 0 to 0
		lda SRC+ROW*40,x
		sta DST+ROW*40,y
!end
		inx
		iny
		cpx #80		; end source offset
		bne loop
		rts
		