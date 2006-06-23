
/*******************************************************************************
 * Signature Construction Macros
 ******************************************************************************/

/*
** These macros can be used to construct signature strings. Hopefully their names
** are a little easier to remember than the single character they correspond to.
** For example, to specify the signature of the method:
**
**	public int read(byte b[], int off, int len);
**
** you could write something like this in C:
**
**	char* readSig = JRISigMethod(JRISigArray(JRISigByte)
**								 JRISigInt
**								 JRISigInt) JRISigInt;
**
** Of course, don't put commas between the types.
*/
#define JRISigArray(T)		"[" T
#define JRISigByte			"B"
#define JRISigChar			"C"
#define JRISigClass(name)	"L" name ";"
#define JRISigFloat			"F"
#define JRISigDouble		"D"
#define JRISigMethod(args)	"(" args ")"
#define JRISigNoArgs		""
#define JRISigInt			"I"
#define JRISigLong			"J"
#define JRISigShort			"S"
#define JRISigVoid			"V"
#define JRISigBoolean		"Z"
