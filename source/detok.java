////////////////////////////////////////////////////////////////////////
// $Id: detok.java,v 1.7 2012/09/04 11:22:38 pgs Exp $
//
// $Log: detok.java,v $
// Revision 1.7  2012/09/04 11:22:38  pgs
// petscii mapping
// /
//
// Revision 1.6  2011/06/28 23:40:14  pgs
// Standardising keycodes - now use PETSCII
//
// Revision 1.5  2007/04/19 08:28:24  pgs
// Refactoring and simplifying/formatting code especially in Machine
//
// Revision 1.4  2006/02/17 01:35:57  ctpgs
// Added trap and resume (guessed it is resume)
//
// Revision 1.3  2006/02/13 00:56:24  ctpgs
// RCS...
//
// Revision 1.2  2006/02/13 00:55:04  ctpgs
// Setting up RCS
//
//
////////////////////////////////////////////////////////////////////////

import java.io.*;

class detok
{

  boolean c64sx=false;

  String tokens0[] = { "","","","","","(wht)","","","","" // starts 0
    /*10*/ ,"","","","","","","","(down)","(rvon)","(home)"
    /*20*/ ,"(del)","","","","","","","","(red)","(rght)" // del is not implemented in ijk64
    /*30*/ ,"(grn)","(blu)"," ","","","","","","","" };

  String tokens1[] = { "end", "for" // starting at 128
    /*130*/ ,"next", "data", "input#", "input", "dim", "read", "let", "goto", "run", "if"
    /*140*/ ,"restore", "gosub", "return", "rem", "stop", "on", "wait", "load", "save", "verify"
    /*150*/ ,"def", "poke", "print#", "print", "cont", "list", "clr", "cmd", "sys", "open"
    /*160*/ ,"close", "get", "new", "tab(", "to", "fn", "spc(", "then", "not", "step"
    /*170*/ ,"+", "-", "*", "/", "^", "and", "or", ">", "=", "<"
    /*180*/ ,"sgn", "int", "abs", "usr", "fre", "pos", "sqr", "rnd", "log", "exp"
    /*190*/ ,"cos", "sin", "tan", "atn", "peek", "len", "str$", "val", "asc", "chr$"
    /*200*/ ,"left$", "right$", "mid$", "go","rgr","rclr","rlum","joy","rdot","dec"
    /*210*/ ,"hex$", "err$", "instr", "else", "resume", "trap"
    /*   */ ,"tron", "troff", "sound", "vol", "auto", "pudef", "graphic", "paint"
            ,"char","box","circle","gshape","sshape","draw","locate","color"
            ,"scnclr","scale","help","do","loop","exit","directory","dsave"
            ,"dload","header","scratch","collect","copy","rename","backup","delete"
            ,"renumber","key","monitor","using","until","while" /*fd*/
  };

  String tokens64sx[] = { // starting at 204
    /*204*/ "key","graphic","scnclr","cicle",
    /*208*/ "draw","region","color","point","sound","char","paint","rpot",
    /*216*/ "rpen","rsnd","rcolr","rgr","rjoy","rdot"
  };
  String tokensExtended[] = {
    /*  0*/  "", "", "bank", "filter", "play", "tempo", "movspr", "sprite", "sprcolor", "rreg"
    /* 10*/ ,"envelope", "sleep", "catalog", "dopen", "append", "dclose","bsave", "bload", "record", "concat"
    /* 20*/ ,"dverify", "dclear", "sprsav", "collision", "begin", "bend", "window", "boot", "width", ""
    /* 30*/ ,"quit", "stash", "", "fetch", "", "swap", "off", "fast", "slow", ""
    /* 40*/ ,"", "", "", "", "", "", "", "", "", ""
    /* 50*/ ,"", "", "", "", "", "", "", "", "", ""
    /* 60*/ ,"", "", "", "", "", "", "", "", "", ""
    /* 70*/ ,"", "", "", "", "", "", "", "", "", ""
    /* 80*/ ,"", "", "", "", "", "", "", "", "", ""
    /* 90*/ ,"", "", "", "", "", "", "", "", "", ""
    /*100*/ ,"", "", "", "", "", "", "", "", "", ""
    /*110*/ ,"", "", "", "", "", "", "", "", "", ""
    /*120*/ ,"", "", "", "", "", "", "", "", "key", "color" // c64superexpander
    /*130*/ ,"graphic", "scnclr", "locate", "scale", "box", "circle", "char", "draw", "gshape", "paint"
    /*140*/ ,"sshape", "tune", "filter", "sprdef", "tempo", "movspr", "sprcol", "sprite", "colint", "sprsav"
    /*150*/ ,"rbump", "rclr", "rdot", "rgr", "rjoy", "rpen", "rpot", "rspcol", "rsppos", "rspr"
    /*160*/ ,"", "", "", "", "", "", "", "", "", ""
    /*170*/ ,"", "", "", "", "", "", "", "", "", ""
    /*180*/ ,"", "", "", "", "", "", "", "", "", ""
    /*190*/ ,"", "", "", "", "", "", "", "", "", ""
    /*200*/ ,"", "", "", "", "", "", "", "", "", ""
    /*210*/ ,"", "", "", "", "", "", "", "", "", ""
    /*220*/ ,"", "", "", "", "", "", "", "", "", ""
    /*230*/ ,"", "", "", "", "", "", "", "", "", ""
    /*240*/ ,"", "", "", "", "", "", "", "", "", ""
    /*250*/ ,"", "", "", "", "", ""
  };

 //String colourname_alias[] = { "blk", "wht", "red", "cyn", "pur", "grn",
    //"blu", "yel", "orng", "brn", "lred", "gry1",
    //"gry2", "lgrn", "lblu", "gry3"
  //};                      

  String tokens1quoted[] = { "", "(orng)" // starting at 128
    /*130*/ ,"", "", "", "(F1)", "", "", "", "", "", ""
    /*140*/ ,"", "", "", "", "(blk)", "(up)", "(rvof)", "(clr)", "", "(brn)"
    /*150*/ ,"(lred)", "(gry1)", "(gry2)", "(lgrn)", "(lblu)", "(gry3)", "(pur)", "(left)", "(yel)", "(cyn)"
    /*160*/ ,"(BLOCK)", "(CBM-K)", "(CBM-I)", "(CBM-T)", "(CBM-@)", "(CBM-G)", "(CBM-PLUS)", "(CBM-M)", "(CBM-POUND)", "(SHIFT-POUND)"
    /*170*/ ,"(CBM-N)", "(CBM-Q)", "(CBM-D)", "(CBM-Z)", "(CBM-S)", "(CBM-P)", "(CBM-A)", "(CBM-E)", "(CBM-R)", "(CBM-W)"
    /*180*/ ,"(CBM-H)", "(CBM-J)", "(CBM-L)", "(CBM-Y)", "(CBM-U)", "(CBM-O)", "(SHIFT-@)", "(CBM-F)", "(CBM-C)", "(CBM-X)"
    /*190*/ ,"(CBM-V)", "(CBM-B)", "(HLINE)", "", "", "", "", "", "", ""   // HLINE did have - // then A-G
    /*200*/ ,"", "", "", "", "", "", "", "", "", "" // H-Q
    /*210*/ ,"", "", "", "", "", "", "", "", "", "(SHIFT-PLUS)" // R-Z 
    /*220*/ ,"", "|", "", "(BACK-TRIANGLE)", "", "", "", "", "", ""  // 223 was _
    /*230*/ ,"", "", "", "", "", "", "", "", "", ""
  };

   // change _ to (anglethingo etc)
   // change @ to (-)
   
  static byte[] read_a_file(String filename) {

    //String content="";
    byte content[];

    content= new byte[0];
    try {
      FileInputStream fis = new FileInputStream(filename);
      int x= fis.available();
      byte b[]= new byte[x];
      fis.read(b);
      content = b;
      //content = new String(b);
      //System.out.println(content);
    } catch (Exception e) { }

    return content;
  }

  detok(String[] args) {
    byte code[] = read_a_file(args[0]);
    //System.out.printf("Code = %s\n",code);
    print_detok(code);
  }

  int convert2(byte a, byte b) {
    return (int)(b&0xff)*256+(int)(a&0xff);
  }

  String changebracket(String in) {
    if (false) {
      if (in.substring(0,1).equals("(")
        && in.substring(in.length()-1,in.length()).equals(")")) {
        // it is surrounded in ( change to { if we want that format
        return "{"+in.substring(1,in.length()-1)+"}";
      }
    }
    return in;
  }

  void print_detok(byte[] code) {
    int mode=0;
    // 0 2bytes expecting a 2 byte int
    // 1 2bytes expecting a 2 byte int
    // 2 2bytes line number
    // 3 text code

    boolean quoted=false;
    for (int pnt=0; pnt<code.length; ++pnt) {
      // 
      if (mode==0) {
        // read two bytes
        byte a=code[pnt++];
        byte b=code[pnt];
        if (false) { System.out.printf("Got int %d\n",convert2(a,b)); }
        mode=1;
      } else if (mode==1) {
        // read two bytes
        byte a=code[pnt++];
        byte b=code[pnt];
        if (false) { System.out.printf("Got int %d\n",convert2(a,b)); }
        // if this is 0 then it is the end of the program
        mode=2;
      } else if (mode==2) {
        // read two bytes
        byte a=code[pnt++];
        byte b=code[pnt];
        if (false) { System.out.printf("Got line # %d\n",convert2(a,b)); }
        System.out.printf("%5d ",convert2(a,b));
        quoted=false;
        mode=3;
      } else {
      byte a=code[pnt];
      if (a>='A' && a<='Z') {
        System.out.printf("%c",a-'A'+'a');
      } else if (quoted && (int)(a&0xff)>192 && (int)(a&0xff)<=192+26) { // exclude 192
        //System.out.printf("{rev-%c}",(char)(((int)(a&0xff)-192-1)+'A'));
        System.out.printf("%c",(char)(((int)(a&0xff)-192-1)+'A'));
      } else if (quoted && (int)(a&0xff)>=128 && (int)(a&0xff)<=223) {
        int tok=(int)(a&0xff)-128;
        if (tokens1quoted[tok].equals("")) {
          System.out.printf("{%d}",(int)(a&0xff));
        } else {
          System.out.printf("%s",changebracket(tokens1quoted[tok]));
        }
      } else if (c64sx && !quoted && (int)(a&0xff)>=204 && (int)(a&0xff)<=221) { // ! quoted
        int tok=(int)(a&0xff)-204;
        if (tokens64sx[tok].equals("")) {
          System.out.printf("{%d}",(int)(a&0xff));
        } else {
          System.out.printf("%s",tokens64sx[tok]);
        }
      } else if (!quoted && (int)(a&0xff)>=128 && (int)(a&0xff)<=253 /*212*/) { // ! quoted
        int tok=(int)(a&0xff)-128;
        if (tokens1[tok].equals("")) {
          System.out.printf("{%d}",(int)(a&0xff));
        } else {
          System.out.printf("%s",tokens1[tok]);
        }
      } else if ((int)(a&0xff)>=1 && (int)(a&0xff)<=31) {
        int tok=(int)(a&0xff)-0;
        if (tokens0[tok].equals("")) {
          System.out.printf("{%d}",(int)(a&0xff));
        } else {
          System.out.printf("%s",changebracket(tokens0[tok]));
        }
      } else if (a=='\"') {
        System.out.printf("%c",a);
        quoted=!quoted;
      } else if ((int)(a&0xff)==255) {
        /*System.out.printf("{pi}");*/
        System.out.printf("(mathpi)");
      } else if ((int)(a&0xff)==92) {
        System.out.printf("|",a);
      } else if ((int)(a&0xff)==91) {
        System.out.printf("[",a); // should be this
      } else if ((int)(a&0xff)==93) {
        //System.out.printf("}",a);
        System.out.printf("]",a); // should be this
      } else if ((int)(a&0xff)==94) {
        System.out.printf("~",a);
      } else if (a=='>'||a=='#'||a=='*'||a=='&'||a=='<'||a=='+'||a=='/'||a=='@'||a=='!'||a=='-'
               ||a=='?'||a=='\''||a=='.'||a=='%'||a=='='||a==';'||a=='$'||a==','||a==' '||a==':'||a=='\"'||a=='('||a==')'||(a>='0'&&a<='9')) {
        System.out.printf("%c",a);
      } else if (a>='a' && a<='z') {
        System.out.printf("[%c]",a);
      } else if ((int)(a&0xff)==95) {
        //System.out.printf("%s",changebracket("(arrow left)"));
        System.out.printf("%c",(char)127); // makes it the same as another detokeniser
      } else if ((int)(a&0xff)==254) {
        // extended
        byte b=code[++pnt];
        if (tokensExtended[(int)(b&0xFF)]!="") {
          System.out.printf("%s",tokensExtended[(int)(b&0xFF)]);
        } else if (false && (int)(b&0xff)==37) {
          System.out.printf("fast");
        } else if (false && (int)(b&0xff)==130) {
          System.out.printf("graphic");
        } else {
          System.out.printf("{%d,",(int)(a&0xff));
          System.out.printf("%d}",(int)(b&0xff));
        }
      } else if ((int)(a&0xff)==0) {
        // end of line
        if (false) { System.out.printf("[END OF LINE]\n"); }
        System.out.printf("\n");
        mode=1;
      } else {
        System.out.printf("{%d}",(int)(a&0xff));
      }
      }
    }
    if (false) { System.out.printf("Finished\n"); }
  }

  ///////////////////////////////
  public static void main(String[] args) {
    if (false) { System.out.printf("Mello Word\n"); }
    // no longer can run it like this - must run it from machine
    new detok(args);
  }

}
