; Joystick en Keyboard routine

*= $0801
!basic 1,debugr

debugr:
		sei			; disable KERNAL keyboard IRQ scanner
		lda #0
		sta $FC
		lda #4
		sta $FD
.loop:
		ldy #0

		; joystick
		lda #$FF
		sta $DC00		; Disconnect keyboard
		lda $DC00		; Joystick A in control port 2 0=active: 1=up 2=down 4=left 8=right 16=fire
		and #%00011111
		eor #%00011111
		jsr puthex
		lda $DC01		; Joystick B in control port 1
		sta controlport1+1
		and #%00011111
		eor #%00011111
		jsr puthex
controlport1:
		lda #00			; SELF-MODIFIED
		cmp #$FF
		bne .joyactive
		
		; keyboard scan
		lda #%01111111		; |Bit 7| R/S |  Q  |  C= |SPACE|  2  | CTRL|A_LFT|  1  |
		sta $DC00
		lda $DC01		; Read Keyboard row
		jsr puthex
		jmp .loop
		
.joyactive:
		; ignore keyboard
		lda #$FF
		jsr puthex
		
		jmp .loop
		
		
; hexadecimal output
; A=input
; (FB/FC) is output location, offset by Y
; assumes PETSCI charset (1-6=A-F, 48-58=digits)
puthex:
		pha
		lsr
		lsr
		lsr
		lsr
		jsr .nibble
		pla
		and #$0F
.nibble:
		clc
		adc #48
		cmp #58
		bmi .isdigit
		sbc #57		; letter
.isdigit
		sta ($FC),y
		iny
		rts
	
;                              Port B - $DC01
;              +-----+-----+-----+-----+-----+-----+-----+-----+
;              |Bit 7|Bit 6|Bit 5|Bit 4|Bit 3|Bit 2|Bit 1|Bit 0|
;        +-----+-----+-----+-----+-----+-----+-----+-----+-----+
;        |Bit 7| R/S |  Q  |  C= |SPACE|  2  | CTRL|A_LFT|  1  |
;        +-----+-----+-----+-----+-----+-----+-----+-----+-----+
;        |Bit 6|  /  | A_UP|  =  | S_R | HOME|  ;  |  *  |POUND|
;        +-----+-----+-----+-----+-----+-----+-----+-----+-----+
;        |Bit 5|  ,  |  @  |  :  |  .  |  -  |  L  |  P  |  +  |
;        +-----+-----+-----+-----+-----+-----+-----+-----+-----+
;        |Bit 4|  N  |  O  |  K  |  M  |  0  |  J  |  I  |  9  |
; Port A +-----+-----+-----+-----+-----+-----+-----+-----+-----+
; $DC00  |Bit 3|  V  |  U  |  H  |  B  |  8  |  G  |  Y  |  7  |
;        +-----+-----+-----+-----+-----+-----+-----+-----+-----+
;        |Bit 2|  X  |  T  |  F  |  C  |  6  |  D  |  R  |  5  |
;        +-----+-----+-----+-----+-----+-----+-----+-----+-----+
;        |Bit 1| S_L |  E  |  S  |  Z  |  4  |  A  |  W  |  3  |
;        +-----+-----+-----+-----+-----+-----+-----+-----+-----+
;        |Bit 0|C_U/D|  F5 |  F3 |  F1 |  F7 |C_L/R|  CR | DEL |
;        +-----+-----+-----+-----+-----+-----+-----+-----+-----+
;
;  C_L/R = Cursor left/right  ;  C_U/D = Cursor up/down
;  S_L = Shift, left  ;  S_R = Shift, right  ;  R/S = RUN/STOP
;  A_UP = Arrow up    ;  A_LFT = Arrow left

