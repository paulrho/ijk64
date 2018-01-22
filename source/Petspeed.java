////////////////////////////////////////////////
//
//
//
////////////////////////////////////////////////
class Petspeed
{
  static final int MAX=5000;
  // store program memory
  int top=0;
  int []prog= new int[MAX]; // program memory instruction
  double [] pargD = new double[MAX]; // double arg
  int [] pargmem = new int[MAX]; // mem pointer arg
  String [] pargS = new String[MAX]; // string arg
  Machine using_machine=null;

  Petspeed(Machine m) {
    using_machine=m;
  }
  // storing the program
  int getTop() { return top; }
  int addInstr(int instr) { return addInstr(instr, 0.0,-1, ""); }
  int addInstr(int instr, String argS) { return addInstr(instr, 0.0,-1, argS); }
  int addInstr(int instr, double argD, int argmem) { return addInstr(instr, argD, argmem, ""); }
  int addInstr(int instr, double argD) { return addInstr(instr, argD, -1, ""); }
  int addInstr(int instr, int argmem)  { return addInstr(instr, 0.0, argmem, ""); }
  int addInstr(int instr, double argD, int argmem, String argS) {
    prog[top]=instr;
    pargD[top]=argD;
    pargmem[top]=argmem;
    pargS[top]=argS;
    if (true) System.out.printf("%d %d %d %f %s\n",top,prog[top],pargmem[top],pargD[top],pargS[top]);
    top++;
    return 1;
  }
  void rewind() {
    top--;
  }
   
  static int ftoken(String f) {
    for (int i=0; i<O_strings.length; ++i)
      if (O_strings[i].equals(f)) return i;
    for (int i=0; i<F_strings.length; ++i)
      if (F_strings[i].equals(f)) return i;
    return -1;
  }

  // Instruction Type Mode Operation Function
  static final int I_PRF=0<<8;
  static final int I_PSH=1<<8;
  static final int I_STO=2<<8;
  static final int I_FNC=3<<8;
  static final int I_HLT=4<<8;

  static final int T_Dbl=0<<6;
  static final int T_Str=1<<6;

  static final int M_IMM=0<<5;
  static final int M_MEM=1<<5;
  static final int M_MEMARR1=2<<5;
  static final int M_MEMARR2=3<<5;

  static final int O_exp=0;
  static final int O_mul=1;
  static final int O_div=2;
  static final int O_add=3;
  static final int O_sub=4;
  static final int O_neg=5;
  static final int O_not=6;
  static final int O_and=7;
  static final int O_or=8;
  static final int O_xor=9;
  static final int O_eq=10;
  static final int O_lt=11;
  static final int O_gt=12;
  static final int O_ge=13;
  static final int O_le=14;

  static final int F_sin=0;
  static final int F_cos=1;
  static final int F_int=2;

  static String O_strings[]={"^","*","/","+","-","-ve","not","and","or","xor","=","<",">",">=","<="};
  static String F_strings[]={"sin","cos","int"};
  //enum { I_PRF, I_PSH, I_STO, I_FNC, I_HLT };           //0..5  (3 bits)

  // enum { T_Dbl, T_Str };                                //0..1  (1 bit)
  // enum { M_IMM, M_MEM, M_MEMARR1, M_MEMARR2 };          //0..3  (2 bits)
  // enum { O_exp, O_mul, O_div, O_add, O_sub, O_neg       //0..15 (4 bits)
  //     ,O_not,O_and,O_or,O_xor
  //       ,O_eq,O_lt,O_gt,O_ge,O_le
  //};
  //enum { F_sin, F_cos, F_int }                         //0..2  but will be 36 - so (5 bits)

}
