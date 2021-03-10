package com.xy.netdev.frame.util.pack;
/**
 * @author Administrator
 * @date 2019/9/30
 */
public enum PackFmt {
//    格式符	C语言类型	Python类型	Standard size
//    x	pad byte(填充字节)	no value
//    c	char	string of length 1	1
//    b	signed char	integer	1
//    B	unsigned char	integer	1
//            ?	_Bool	bool	1
//    h	short	integer	2
//    H	unsigned short	integer	2
//    i	int	integer	4
//    I(大写的i)	unsigned int	integer	4
//    l(小写的L)	long	integer	4
//    L	unsigned long	long	4
//    q	long long	long	8
//    Q	unsigned long long	long	8
//    f	float	float	4
//    d	double	float	8
//    s	char[]	string
//    p	char[]	string
//    P	void *	long
    //单字节
    c('c',1),
    //单字节
    b('b',1),
    //单字节
    B('B',1),
    //双子杰
    h('h',2),
    //双子杰
    H('H',2),
    //四字节
    i('i',4),
    //四字节
    I('I',4),
    //四字节
    l('l',4),
    //四字节
    L('L',4),
    //八字节
    q('q',8),
    //八字节
    Q('Q',8);

    private final char k;
    private final int v;

    public char k() {
        return k;
    }

    public int v() {
        return v;
    }

    PackFmt(char k,int v){
        this.k=k;
        this.v=v;
    }
}
