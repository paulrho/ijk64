////////////////////////////////////////////////
//
//
//
////////////////////////////////////////////////
class Petspeed
{
  static final int MAX=5000;
  // store program memory
  int top=1; // because 0 is a special pointer to show invalid
  int tmptop;
  boolean record=false;
  int []prog= new int[MAX]; // program memory instruction
  double [] pargD = new double[MAX]; // double arg
  int [] pargmem = new int[MAX]; // mem pointer arg
  String [] pargS = new String[MAX]; // string arg
  Machine using_machine=null;

  static final int MAXPSIZE=50000; // just for now, have a more efficient sparse array later
  int []acpointer = new int[MAXPSIZE];
  int []acpointer_next = new int[MAXPSIZE];

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
    if (record) {
      prog[tmptop]=instr;
      pargD[tmptop]=argD;
      pargmem[tmptop]=argmem;
      pargS[tmptop]=argS;
      if (using_machine.verbose) System.out.printf("%d %d %d %f %s\n",tmptop,prog[tmptop],pargmem[tmptop],pargD[tmptop],pargS[tmptop]);
      tmptop++;
      return 1;
    } else return 0;
  }
  void rewind() {
    tmptop--;
  }
   
  static int ftoken(String f) {
    for (int i=0; i<O_strings.length; ++i)
      if (O_strings[i].equals(f)) return i;
    for (int i=0; i<F_strings.length; ++i)
      if (F_strings[i].equals(f)) return i;
    System.out.printf("A-COMPILER could not find %s\n",f);
    return -1;
  }

  //////////////////////////////////////////////////////////////////////
  /// the a-machine
  //////////////////////////////////////////////////////////////////////
  static final int AMAX=100;
  double astack_d[]=new double[AMAX];
  String astack_s[]=new String[AMAX];
  int atop=0;

  boolean execute(int x) throws EvaluateException {
    boolean verbose=using_machine.verbose;
    for (int i=acpointer[x]; i<MAX; ++i) {
      //if (prog[i]==I_HLT) break;
      if (verbose) System.out.printf("EXECUTING %d %d %d %f %s  ",i,prog[i],pargmem[i],pargD[i],pargS[i]);
      switch(prog[i]) {
        case I_HLT: 
          if (verbose) System.out.printf("\n");
	  if (atop!=0 && atop!=1) System.out.printf("atop not at zero or one\n");
          return true;
	case I_FNC | F_sin : 
	  astack_d[atop-1]= Math.sin(astack_d[atop-1]);
	  break;
	case I_FNC | F_cos : 
	  astack_d[atop-1]= Math.cos(astack_d[atop-1]);
	  break;
	case I_FNC | F_int : 
	  astack_d[atop-1]= (double)((int)(astack_d[atop-1]));
	  break;
	case I_FNC | F_log : 
	  astack_d[atop-1]= Math.log(astack_d[atop-1]);
	  break;
	case I_FNC | F_tan : 
	  astack_d[atop-1]= Math.tan(astack_d[atop-1]);
	  break;
	case I_FNC | F_atn : 
	  astack_d[atop-1]= Math.atan(astack_d[atop-1]);
	  break;
	case I_FNC | F_sqr : 
	case I_FNC | F_sqrt : 
	  astack_d[atop-1]= Math.sqrt(astack_d[atop-1]);
	  break;
	case I_FNC | F_asin : 
	  astack_d[atop-1]= Math.asin(astack_d[atop-1]);
	  break;
	case I_FNC | F_acos : 
	  astack_d[atop-1]= Math.acos(astack_d[atop-1]);
	  break;
	case I_FNC | F_abs : 
	  astack_d[atop-1]= Math.abs(astack_d[atop-1]);
	  break;
	case I_FNC | F_rnd : 
          if (astack_d[atop-1]<0) { using_machine.evaluate_engine.generator = new java.util.Random((long)-astack_d[atop-1]); } // added this in for seeds
	  astack_d[atop-1]=using_machine.evaluate_engine.generator.nextDouble();
	  break;
	case I_FNC | F_exp : 
	  astack_d[atop-1]= Math.exp(astack_d[atop-1]);
	  break;

	case I_FNC | F_midD : 
          try {          
	    astack_s[atop-3]=astack_s[atop-3].substring((int)astack_d[atop-2]-1,(int)astack_d[atop-2]-1+(int)astack_d[atop-1]);
	    atop--;
	    atop--;
          } catch(Exception e) {
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
	  break;

	case I_PRF | O_pow : 
	  astack_d[atop-2]= Math.pow(astack_d[atop-2],astack_d[atop-1]); atop--;
	  break;
	case I_PRF | O_neg : 
	  astack_d[atop-1]= -astack_d[atop-1];
	  break;
	case I_PRF | O_mul : 
	  astack_d[atop-2]= astack_d[atop-2] * astack_d[atop-1]; atop--;
	  break;
	case I_PRF | O_add : 
	  astack_d[atop-2]= astack_d[atop-2] + astack_d[atop-1]; atop--;
	  break;
	case I_PRF | O_sub : 
	  astack_d[atop-2]= astack_d[atop-2] - astack_d[atop-1]; atop--;
	  break;
	case I_PRF | O_div : 
	  astack_d[atop-2]= astack_d[atop-2] / astack_d[atop-1]; atop--;
	  break;
	case I_PRF | O_lt : 
	  astack_d[atop-2]= (astack_d[atop-2] < astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_le : 
	  astack_d[atop-2]= (astack_d[atop-2] <= astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_eq : 
	  astack_d[atop-2]= (astack_d[atop-2] == astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_ge : 
	  astack_d[atop-2]= (astack_d[atop-2] >= astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_gt : 
	  astack_d[atop-2]= (astack_d[atop-2] > astack_d[atop-1])?-1:0; atop--;
	  break;
	case I_PRF | O_ne : 
	  astack_d[atop-2]= (astack_d[atop-2] != astack_d[atop-1])?-1:0; atop--;
	  break;
         //if (oper.equals("or")) { answer=(double)(((int)(left)) | ((int)(right))); }
    //else if (oper.equals("xor")) { answer=(double)(((int)(left)) ^ ((int)(right))); }
    //else if (oper.equals("and")) { answer=(double)(((int)(left)) & ((int)(right))); }
    //else if (oper.equals("not")) { answer=(double)(~(int)(right)); }
	case I_PRF | O_not : 
	  astack_d[atop-1]= (double)(~(int)(astack_d[atop-1]));
	  break;
	case I_PRF | O_and : 
	  astack_d[atop-2]= (((int)(astack_d[atop-2])) & ((int)(astack_d[atop-1]))); atop--;
	  break;
	case I_PRF | O_or : 
	  astack_d[atop-2]= (((int)(astack_d[atop-2])) | ((int)(astack_d[atop-1]))); atop--;
	  break;
	case I_PRF | O_xor : 
	  astack_d[atop-2]= (((int)(astack_d[atop-2])) ^ ((int)(astack_d[atop-1]))); atop--;
	  break;

	case I_PRF | T_Str | O_ne : 
	  astack_d[atop-2]= (astack_s[atop-2].equals(astack_s[atop-1]))?0:-1; atop--;
	  break;
	case I_PRF | T_Str | O_eq : 
	  astack_d[atop-2]= (astack_s[atop-2].equals(astack_s[atop-1]))?-1:0; atop--;
	  break;
	case I_PSH | T_Str | M_IMM : 
	  astack_s[atop++]=pargS[i];
	  break;
	case I_PSH | T_Str | M_MEM : 
	  astack_s[atop++]=using_machine.variables.variablestring[pargmem[i]];
	  break;
	case I_PSH | T_Str | M_MEMARR1 : 
	  astack_s[atop-1]=using_machine.variables.variablearraystring1[pargmem[i]][(int)astack_d[atop-1]];
	  break;
	case I_PSH | T_Str | M_MEMARR2 : 
	  astack_s[atop-2]=using_machine.variables.variablearraystring2[pargmem[i]][(int)astack_d[atop-2]][(int)astack_d[atop-1]]; atop--;
	  break;
	case I_STO | T_Str | M_MEM : 
	  using_machine.variables.variablestring[pargmem[i]]=astack_s[--atop];
	  break;
	case I_STO | T_Str | M_MEMARR1 : 
	  using_machine.variables.variablearraystring1[pargmem[i]][(int)astack_d[atop-2]]=astack_s[atop-1];
	  atop--; atop--;
	  break;
	case I_STO | T_Str | M_MEMARR2 : 
	  using_machine.variables.variablearraystring2[pargmem[i]][(int)astack_d[atop-3]][(int)astack_d[atop-2]]=astack_s[atop-1];
	  atop--; atop--; atop--;
	  break;

	case I_PSH | T_Dbl | M_IMM : 
	  astack_d[atop++]=pargD[i];
	  break;
	case I_PSH | T_Dbl | M_MEM : 
	  astack_d[atop++]=using_machine.variables.variablevalue[pargmem[i]];
	  break;
	case I_PSH | T_Dbl | M_MEMARR1 : 
	  astack_d[atop-1]=using_machine.variables.variablearrayvalue1[pargmem[i]][(int)astack_d[atop-1]];
	  break;
	case I_PSH | T_Dbl | M_MEMARR2 : 
	  astack_d[atop-2]=using_machine.variables.variablearrayvalue2[pargmem[i]][(int)astack_d[atop-2]][(int)astack_d[atop-1]];
          atop--;
	  break;
	case I_STO | T_Dbl | M_MEM : 
	  using_machine.variables.variablevalue[pargmem[i]]=astack_d[--atop];
	  break;
	case I_STO | T_Dbl | M_MEMARR1 : 
	  using_machine.variables.variablearrayvalue1[pargmem[i]][(int)astack_d[atop-2]]=astack_d[atop-1];
	  atop--; atop--;
	  break;
	case I_STO | T_Dbl | M_MEMARR2 : 
	  using_machine.variables.variablearrayvalue2[pargmem[i]][(int)astack_d[atop-3]][(int)astack_d[atop-2]]=astack_d[atop-1];
	  atop--; atop--; atop--;
	  break;
	default:
          if (verbose) System.out.printf("X ");
          System.out.printf("Instruction Fault\n");
	  // should through an error! Instruction Fault
	  break;
      }
      if (verbose) System.out.printf("\n");
    }
    return true;
  }

  double result()
  {
    // just pop the last result
	  return astack_d[--atop];
  }
  //////////////////////////////////////////////////////////////////////
  ///
  //////////////////////////////////////////////////////////////////////

  int nextpnt(int x) { return acpointer_next[x]; }
  int savestart_p=0;
  int savestart_ac=0;
  boolean is_compiled(int x) { return acpointer[x]!=0; }
  void savestart(int x) { savestart_p=x; savestart_ac=top; tmptop=top; record=true; }
  void saveacode(int end) 
  {
	  // push a HLT onto code
	  addInstr(I_HLT); 
	  acpointer[savestart_p]=savestart_ac;
	  acpointer_next[savestart_p]=end;
	  // this is now marked as a valid piece of code
	  top=tmptop;
          if (using_machine.verbose) System.out.printf("Saved code from %d to %d for location %d\n",savestart_ac,top,savestart_p);
	  record=false;
  }


  // Instruction Type Mode Operation Function
  static final int I_PRF=0<<8;
  static final int I_PSH=1<<8;
  static final int I_STO=2<<8;
  static final int I_FNC=3<<8;
  static final int I_HLT=4<<8;

  static final int T_Dbl=0<<7;
  static final int T_Str=1<<7;

  static final int M_IMM=0<<5;
  static final int M_MEM=1<<5;
  static final int M_MEMARR1=2<<5;
  static final int M_MEMARR2=3<<5;

  static final int O_pow=0;
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
  static final int O_ne=15;

  static final int F_sin=0;
  static final int F_cos=1;
  static final int F_int=2;
  static final int F_log=3;
  static final int F_sqr=4;
  static final int F_sqrt=5;
  static final int F_atn=6;
  static final int F_tan=7;
  static final int F_asin=8;
  static final int F_acos=9;
  static final int F_abs=10;
  static final int F_rnd=11;
  static final int F_exp=12;
  static final int F_len=13;
  static final int F_val=14;
  static final int F_asc=15;
  static final int F_midD=16;
  static final int F_leftD=17;
  static final int F_rightD=18;
  static final int F_strD=19;
  static final int F_chrD=20;

  static String O_strings[]={"^","*","/","+","-","-ve","not","and","or","xor","=","<",">",">=","<=","<>"};
  static String F_strings[]={"sin","cos","int","log","sqr","sqrt","atn","tan","asin","acos","abs","rnd","exp",
	                     "len","val","asc","mid$","left$","right$","str$","chr$"};
  //enum { I_PRF, I_PSH, I_STO, I_FNC, I_HLT };           //0..5  (3 bits)

  // enum { T_Dbl, T_Str };                                //0..1  (1 bit)
  // enum { M_IMM, M_MEM, M_MEMARR1, M_MEMARR2 };          //0..3  (2 bits)
  // enum { O_exp, O_mul, O_div, O_add, O_sub, O_neg       //0..15 (4 bits)
  //     ,O_not,O_and,O_or,O_xor
  //       ,O_eq,O_lt,O_gt,O_ge,O_le
  //};
  //enum { F_sin, F_cos, F_int }                         //0..2  but will be 36 - so (5 bits)

}