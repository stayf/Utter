package com.stayfprod.emojicon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

public final class EmojiconHandler {
    private EmojiconHandler() {
    }

    private static final SparseIntArray sEmojisMap = new SparseIntArray(846);

    static {
        // People
        sEmojisMap.put(0x1f604, 1);
        sEmojisMap.put(0x1f603, 2);
        sEmojisMap.put(0x1f600, 3);
        sEmojisMap.put(0x1f60a, 4);
        sEmojisMap.put(0x263a, 5);
        sEmojisMap.put(0x1f609, 6);
        sEmojisMap.put(0x1f60d, 7);
        sEmojisMap.put(0x1f618, 8);
        sEmojisMap.put(0x1f61a, 9);
        sEmojisMap.put(0x1f617, 10);
        sEmojisMap.put(0x1f619, 11);
        sEmojisMap.put(0x1f61c, 12);
        sEmojisMap.put(0x1f61d, 13);
        sEmojisMap.put(0x1f61b, 14);
        sEmojisMap.put(0x1f633, 15);
        sEmojisMap.put(0x1f601, 16);
        sEmojisMap.put(0x1f614, 17);
        sEmojisMap.put(0x1f60c, 18);
        sEmojisMap.put(0x1f612, 19);
        sEmojisMap.put(0x1f61e, 20);
        sEmojisMap.put(0x1f623, 21);
        sEmojisMap.put(0x1f622, 22);
        sEmojisMap.put(0x1f602, 23);
        sEmojisMap.put(0x1f62d, 24);
        sEmojisMap.put(0x1f62a, 25);
        sEmojisMap.put(0x1f625, 26);
        sEmojisMap.put(0x1f630, 27);
        sEmojisMap.put(0x1f605, 28);
        sEmojisMap.put(0x1f613, 29);
        sEmojisMap.put(0x1f629, 30);
        sEmojisMap.put(0x1f62b, 31);
        sEmojisMap.put(0x1f628, 32);
        sEmojisMap.put(0x1f631, 33);
        sEmojisMap.put(0x1f620, 34);
        sEmojisMap.put(0x1f621, 35);
        sEmojisMap.put(0x1f624, 36);
        sEmojisMap.put(0x1f616, 37);
        sEmojisMap.put(0x1f606, 38);
        sEmojisMap.put(0x1f60b, 39);
        sEmojisMap.put(0x1f637, 40);
        sEmojisMap.put(0x1f60e, 41);
        sEmojisMap.put(0x1f634, 42);
        sEmojisMap.put(0x1f635, 43);
        sEmojisMap.put(0x1f632, 44);
        sEmojisMap.put(0x1f61f, 45);
        sEmojisMap.put(0x1f626, 46);
        sEmojisMap.put(0x1f627, 47);
        sEmojisMap.put(0x1f608, 48);
        sEmojisMap.put(0x1f47f, 49);
        sEmojisMap.put(0x1f62e, 50);
        sEmojisMap.put(0x1f62c, 51);
        sEmojisMap.put(0x1f610, 52);
        sEmojisMap.put(0x1f615, 53);
        sEmojisMap.put(0x1f62f, 54);
        sEmojisMap.put(0x1f636, 55);
        sEmojisMap.put(0x1f607, 56);
        sEmojisMap.put(0x1f60f, 57);
        sEmojisMap.put(0x1f611, 58);
        sEmojisMap.put(0x1f472, 59);
        sEmojisMap.put(0x1f473, 60);
        sEmojisMap.put(0x1f46e, 61);
        sEmojisMap.put(0x1f477, 62);
        sEmojisMap.put(0x1f482, 63);
        sEmojisMap.put(0x1f476, 64);
        sEmojisMap.put(0x1f466, 65);
        sEmojisMap.put(0x1f467, 66);
        sEmojisMap.put(0x1f468, 67);
        sEmojisMap.put(0x1f469, 68);
        sEmojisMap.put(0x1f474, 69);
        sEmojisMap.put(0x1f475, 70);
        sEmojisMap.put(0x1f471, 71);
        sEmojisMap.put(0x1f47c, 72);
        sEmojisMap.put(0x1f478, 73);
        sEmojisMap.put(0x1f63a, 74);
        sEmojisMap.put(0x1f638, 75);
        sEmojisMap.put(0x1f63b, 76);
        sEmojisMap.put(0x1f63d, 77);
        sEmojisMap.put(0x1f63c, 78);
        sEmojisMap.put(0x1f640, 79);
        sEmojisMap.put(0x1f63f, 80);
        sEmojisMap.put(0x1f639, 81);
        sEmojisMap.put(0x1f63e, 82);
        sEmojisMap.put(0x1f479, 83);
        sEmojisMap.put(0x1f47a, 84);
        sEmojisMap.put(0x1f648, 85);
        sEmojisMap.put(0x1f649, 86);
        sEmojisMap.put(0x1f64a, 87);
        sEmojisMap.put(0x1f480, 88);
        sEmojisMap.put(0x1f47d, 89);
        sEmojisMap.put(0x1f4a9, 90);
        sEmojisMap.put(0x1f525, 91);
        sEmojisMap.put(0x2728, 92);
        sEmojisMap.put(0x1f31f, 93);
        sEmojisMap.put(0x1f4ab, 94);
        sEmojisMap.put(0x1f4a5, 95);
        sEmojisMap.put(0x1f4a2, 96);
        sEmojisMap.put(0x1f4a6, 97);
        sEmojisMap.put(0x1f4a7, 98);
        sEmojisMap.put(0x1f4a4, 99);
        sEmojisMap.put(0x1f4a8, 100);
        sEmojisMap.put(0x1f442, 101);
        sEmojisMap.put(0x1f440, 102);
        sEmojisMap.put(0x1f443, 103);
        sEmojisMap.put(0x1f445, 104);
        sEmojisMap.put(0x1f444, 105);
        sEmojisMap.put(0x1f44d, 106);
        sEmojisMap.put(0x1f44e, 107);
        sEmojisMap.put(0x1f44c, 108);
        sEmojisMap.put(0x1f44a, 109);
        sEmojisMap.put(0x270a, 110);
        sEmojisMap.put(0x270c, 111);
        sEmojisMap.put(0x1f44b, 112);
        sEmojisMap.put(0x270b, 113);
        sEmojisMap.put(0x1f450, 114);
        sEmojisMap.put(0x1f446, 115);
        sEmojisMap.put(0x1f447, 116);
        sEmojisMap.put(0x1f449, 117);
        sEmojisMap.put(0x1f448, 118);
        sEmojisMap.put(0x1f64c, 119);
        sEmojisMap.put(0x1f64f, 120);
        sEmojisMap.put(0x261d, 121);
        sEmojisMap.put(0x1f44f, 122);
        sEmojisMap.put(0x1f4aa, 123);
        sEmojisMap.put(0x1f6b6, 124);
        sEmojisMap.put(0x1f3c3, 125);
        sEmojisMap.put(0x1f483, 126);
        sEmojisMap.put(0x1f46b, 127);
        sEmojisMap.put(0x1f46a, 128);//
        sEmojisMap.put(0x1f46c, 129);
        sEmojisMap.put(0x1f46d, 130);
        sEmojisMap.put(0x1f48f, 131);
        sEmojisMap.put(0x1f491, 132);
        sEmojisMap.put(0x1f46f, 133);
        sEmojisMap.put(0x1f646, 134);
        sEmojisMap.put(0x1f645, 135);
        sEmojisMap.put(0x1f481, 136);
        sEmojisMap.put(0x1f64b, 137);
        sEmojisMap.put(0x1f486, 138);
        sEmojisMap.put(0x1f487, 139);
        sEmojisMap.put(0x1f485, 140);
        sEmojisMap.put(0x1f470, 141);
        sEmojisMap.put(0x1f64e, 142);
        sEmojisMap.put(0x1f64d, 143);
        sEmojisMap.put(0x1f647, 144);
        sEmojisMap.put(0x1f3a9, 145);
        sEmojisMap.put(0x1f451, 146);
        sEmojisMap.put(0x1f452, 147);
        sEmojisMap.put(0x1f45f, 148);
        sEmojisMap.put(0x1f45e, 149);
        sEmojisMap.put(0x1f461, 150);
        sEmojisMap.put(0x1f460, 151);
        sEmojisMap.put(0x1f462, 152);
        sEmojisMap.put(0x1f455, 153);
        sEmojisMap.put(0x1f454, 154);
        sEmojisMap.put(0x1f45a, 155);
        sEmojisMap.put(0x1f457, 156);
        sEmojisMap.put(0x1f3bd, 157);
        sEmojisMap.put(0x1f456, 158);
        sEmojisMap.put(0x1f458, 159);
        sEmojisMap.put(0x1f459, 160);
        sEmojisMap.put(0x1f4bc, 161);
        sEmojisMap.put(0x1f45c, 162);
        sEmojisMap.put(0x1f45d, 163);
        sEmojisMap.put(0x1f45b, 164);
        sEmojisMap.put(0x1f453, 165);
        sEmojisMap.put(0x1f380, 166);
        sEmojisMap.put(0x1f302, 167);
        sEmojisMap.put(0x1f484, 168);
        sEmojisMap.put(0x1f49b, 169);
        sEmojisMap.put(0x1f499, 170);
        sEmojisMap.put(0x1f49c, 171);
        sEmojisMap.put(0x1f49a, 172);
        sEmojisMap.put(0x2764, 173);
        sEmojisMap.put(0x1f494, 174);
        sEmojisMap.put(0x1f497, 175);
        sEmojisMap.put(0x1f493, 176);
        sEmojisMap.put(0x1f495, 177);
        sEmojisMap.put(0x1f496, 178);
        sEmojisMap.put(0x1f49e, 179);
        sEmojisMap.put(0x1f498, 180);
        sEmojisMap.put(0x1f48c, 181);
        sEmojisMap.put(0x1f48b, 182);
        sEmojisMap.put(0x1f48d, 183);
        sEmojisMap.put(0x1f48e, 184);
        sEmojisMap.put(0x1f464, 185);
        sEmojisMap.put(0x1f465, 186);
        sEmojisMap.put(0x1f4ac, 187);
        sEmojisMap.put(0x1f463, 188);
        sEmojisMap.put(0x1f4ad, 189);

        // Nature
        sEmojisMap.put(0x1f436, 300);
        sEmojisMap.put(0x1f43a, 301);
        sEmojisMap.put(0x1f431, 302);
        sEmojisMap.put(0x1f42d, 303);
        sEmojisMap.put(0x1f439, 304);
        sEmojisMap.put(0x1f430, 305);
        sEmojisMap.put(0x1f438, 306);
        sEmojisMap.put(0x1f42f, 307);
        sEmojisMap.put(0x1f428, 308);
        sEmojisMap.put(0x1f43b, 309);
        sEmojisMap.put(0x1f437, 310);
        sEmojisMap.put(0x1f43d, 311);
        sEmojisMap.put(0x1f42e, 312);
        sEmojisMap.put(0x1f417, 313);
        sEmojisMap.put(0x1f435, 314);
        sEmojisMap.put(0x1f412, 315);
        sEmojisMap.put(0x1f434, 316);
        sEmojisMap.put(0x1f411, 317);
        sEmojisMap.put(0x1f418, 318);
        sEmojisMap.put(0x1f43c, 319);
        sEmojisMap.put(0x1f427, 320);
        sEmojisMap.put(0x1f426, 321);
        sEmojisMap.put(0x1f424, 322);
        sEmojisMap.put(0x1f425, 323);
        sEmojisMap.put(0x1f423, 324);
        sEmojisMap.put(0x1f414, 325);
        sEmojisMap.put(0x1f40d, 326);
        sEmojisMap.put(0x1f422, 327);
        sEmojisMap.put(0x1f41b, 328);
        sEmojisMap.put(0x1f41d, 329);
        sEmojisMap.put(0x1f41c, 330);
        sEmojisMap.put(0x1f41e, 331);
        sEmojisMap.put(0x1f40c, 332);
        sEmojisMap.put(0x1f419, 333);
        sEmojisMap.put(0x1f41a, 334);
        sEmojisMap.put(0x1f420, 335);
        sEmojisMap.put(0x1f41f, 336);
        sEmojisMap.put(0x1f42c, 337);
        sEmojisMap.put(0x1f433, 338);
        sEmojisMap.put(0x1f40b, 339);
        sEmojisMap.put(0x1f404, 340);
        sEmojisMap.put(0x1f40f, 341);
        sEmojisMap.put(0x1f400, 342);
        sEmojisMap.put(0x1f403, 343);
        sEmojisMap.put(0x1f405, 344);
        sEmojisMap.put(0x1f407, 345);
        sEmojisMap.put(0x1f409, 346);
        sEmojisMap.put(0x1f40e, 347);
        sEmojisMap.put(0x1f410, 348);
        sEmojisMap.put(0x1f413, 349);
        sEmojisMap.put(0x1f415, 350);
        sEmojisMap.put(0x1f416, 351);
        sEmojisMap.put(0x1f401, 352);
        sEmojisMap.put(0x1f402, 353);
        sEmojisMap.put(0x1f432, 354);
        sEmojisMap.put(0x1f421, 355);
        sEmojisMap.put(0x1f40a, 356);
        sEmojisMap.put(0x1f42b, 357);
        sEmojisMap.put(0x1f42a, 358);
        sEmojisMap.put(0x1f406, 359);
        sEmojisMap.put(0x1f408, 360);
        sEmojisMap.put(0x1f429, 361);
        sEmojisMap.put(0x1f43e, 362);
        sEmojisMap.put(0x1f490, 363);
        sEmojisMap.put(0x1f338, 364);
        sEmojisMap.put(0x1f337, 365);
        sEmojisMap.put(0x1f340, 366);
        sEmojisMap.put(0x1f339, 367);
        sEmojisMap.put(0x1f33b, 368);
        sEmojisMap.put(0x1f33a, 369);
        sEmojisMap.put(0x1f341, 370);
        sEmojisMap.put(0x1f343, 371);
        sEmojisMap.put(0x1f342, 372);
        sEmojisMap.put(0x1f33f, 373);
        sEmojisMap.put(0x1f33e, 374);
        sEmojisMap.put(0x1f344, 375);
        sEmojisMap.put(0x1f335, 376);
        sEmojisMap.put(0x1f334, 377);
        sEmojisMap.put(0x1f332, 378);
        sEmojisMap.put(0x1f333, 379);
        sEmojisMap.put(0x1f330, 380);
        sEmojisMap.put(0x1f331, 381);
        sEmojisMap.put(0x1f33c, 382);
        sEmojisMap.put(0x1f310, 383);
        sEmojisMap.put(0x1f31e, 384);
        sEmojisMap.put(0x1f31d, 385);
        sEmojisMap.put(0x1f31a, 386);
        sEmojisMap.put(0x1f311, 387);
        sEmojisMap.put(0x1f312, 388);
        sEmojisMap.put(0x1f313, 389);
        sEmojisMap.put(0x1f314, 390);
        sEmojisMap.put(0x1f315, 391);
        sEmojisMap.put(0x1f316, 392);
        sEmojisMap.put(0x1f317, 393);
        sEmojisMap.put(0x1f318, 394);
        sEmojisMap.put(0x1f31c, 395);
        sEmojisMap.put(0x1f31b, 396);
        sEmojisMap.put(0x1f319, 397);
        sEmojisMap.put(0x1f30d, 398);
        sEmojisMap.put(0x1f30e, 399);
        sEmojisMap.put(0x1f30f, 400);
        sEmojisMap.put(0x1f30b, 401);
        sEmojisMap.put(0x1f30c, 402);
        sEmojisMap.put(0x1f320, 403); // TODO (rockerhieu) review this emoji
        sEmojisMap.put(0x2b50, 404);
        sEmojisMap.put(0x2600, 405);
        sEmojisMap.put(0x26c5, 406);
        sEmojisMap.put(0x2601, 407);
        sEmojisMap.put(0x26a1, 408);
        sEmojisMap.put(0x2614, 409);
        sEmojisMap.put(0x2744, 410);
        sEmojisMap.put(0x26c4, 411);
        sEmojisMap.put(0x1f300, 412);
        sEmojisMap.put(0x1f301, 413);
        sEmojisMap.put(0x1f308, 414);
        sEmojisMap.put(0x1f30a, 415);

        // Objects
        sEmojisMap.put(0x1f38d, 600);
        sEmojisMap.put(0x1f49d, 601);
        sEmojisMap.put(0x1f38e, 602);
        sEmojisMap.put(0x1f392, 603);
        sEmojisMap.put(0x1f393, 604);
        sEmojisMap.put(0x1f38f, 605);
        sEmojisMap.put(0x1f386, 606);
        sEmojisMap.put(0x1f387, 607);
        sEmojisMap.put(0x1f390, 608);
        sEmojisMap.put(0x1f391, 609);
        sEmojisMap.put(0x1f383, 610);
        sEmojisMap.put(0x1f47b, 611);
        sEmojisMap.put(0x1f385, 612);
        sEmojisMap.put(0x1f384, 613);
        sEmojisMap.put(0x1f381, 614);
        sEmojisMap.put(0x1f38b, 615);
        sEmojisMap.put(0x1f389, 616);
        sEmojisMap.put(0x1f38a, 617);
        sEmojisMap.put(0x1f388, 618);
        sEmojisMap.put(0x1f38c, 619);
        sEmojisMap.put(0x1f52e, 620);
        sEmojisMap.put(0x1f3a5, 621);
        sEmojisMap.put(0x1f4f7, 622);
        sEmojisMap.put(0x1f4f9, 623);
        sEmojisMap.put(0x1f4fc, 624);
        sEmojisMap.put(0x1f4bf, 625);
        sEmojisMap.put(0x1f4c0, 626);
        sEmojisMap.put(0x1f4bd, 627);
        sEmojisMap.put(0x1f4be, 628);
        sEmojisMap.put(0x1f4bb, 629);
        sEmojisMap.put(0x1f4f1, 630);
        sEmojisMap.put(0x260e, 631);
        sEmojisMap.put(0x1f4de, 632);
        sEmojisMap.put(0x1f4df, 633);
        sEmojisMap.put(0x1f4e0, 634);
        sEmojisMap.put(0x1f4e1, 635);
        sEmojisMap.put(0x1f4fa, 636);
        sEmojisMap.put(0x1f4fb, 637);
        sEmojisMap.put(0x1f50a, 638);
        sEmojisMap.put(0x1f509, 639);
        sEmojisMap.put(0x1f508, 640); // TODO (rockerhieu): review this emoji
        sEmojisMap.put(0x1f507, 641);
        sEmojisMap.put(0x1f514, 642);
        sEmojisMap.put(0x1f515, 643);
        sEmojisMap.put(0x1f4e2, 644);
        sEmojisMap.put(0x1f4e3, 645);
        sEmojisMap.put(0x23f3, 646);
        sEmojisMap.put(0x231b, 647);
        sEmojisMap.put(0x23f0, 648);
        sEmojisMap.put(0x231a, 649);
        sEmojisMap.put(0x1f513, 650);
        sEmojisMap.put(0x1f512, 651);
        sEmojisMap.put(0x1f50f, 652);
        sEmojisMap.put(0x1f510, 653);
        sEmojisMap.put(0x1f511, 654);
        sEmojisMap.put(0x1f50e, 655);
        sEmojisMap.put(0x1f4a1, 656);
        sEmojisMap.put(0x1f526, 657);
        sEmojisMap.put(0x1f506, 658);
        sEmojisMap.put(0x1f505, 659);
        sEmojisMap.put(0x1f50c, 660);
        sEmojisMap.put(0x1f50b, 661);
        sEmojisMap.put(0x1f50d, 662);
        sEmojisMap.put(0x1f6c1, 663);
        sEmojisMap.put(0x1f6c0, 664);
        sEmojisMap.put(0x1f6bf, 665);
        sEmojisMap.put(0x1f6bd, 666);
        sEmojisMap.put(0x1f527, 667);
        sEmojisMap.put(0x1f529, 668);
        sEmojisMap.put(0x1f528, 669);
        sEmojisMap.put(0x1f6aa, 670);
        sEmojisMap.put(0x1f6ac, 671);
        sEmojisMap.put(0x1f4a3, 672);
        sEmojisMap.put(0x1f52b, 673);
        sEmojisMap.put(0x1f52a, 674);
        sEmojisMap.put(0x1f48a, 675);
        sEmojisMap.put(0x1f489, 676);
        sEmojisMap.put(0x1f4b0, 677);
        sEmojisMap.put(0x1f4b4, 678);
        sEmojisMap.put(0x1f4b5, 679);
        sEmojisMap.put(0x1f4b7, 680);
        sEmojisMap.put(0x1f4b6, 681);
        sEmojisMap.put(0x1f4b3, 682);
        sEmojisMap.put(0x1f4b8, 683);
        sEmojisMap.put(0x1f4f2, 684);
        sEmojisMap.put(0x1f4e7, 685);
        sEmojisMap.put(0x1f4e5, 686);
        sEmojisMap.put(0x1f4e4, 687);
        sEmojisMap.put(0x2709, 688);
        sEmojisMap.put(0x1f4e9, 689);
        sEmojisMap.put(0x1f4e8, 690);
        sEmojisMap.put(0x1f4ef, 691);
        sEmojisMap.put(0x1f4eb, 692);
        sEmojisMap.put(0x1f4ea, 693);
        sEmojisMap.put(0x1f4ec, 694);
        sEmojisMap.put(0x1f4ed, 695);
        sEmojisMap.put(0x1f4ee, 696);
        sEmojisMap.put(0x1f4e6, 697);
        sEmojisMap.put(0x1f4dd, 698);
        sEmojisMap.put(0x1f4c4, 699);
        sEmojisMap.put(0x1f4c3, 700);
        sEmojisMap.put(0x1f4d1, 701);
        sEmojisMap.put(0x1f4ca, 702);
        sEmojisMap.put(0x1f4c8, 703);
        sEmojisMap.put(0x1f4c9, 704);
        sEmojisMap.put(0x1f4dc, 705);
        sEmojisMap.put(0x1f4cb, 706);
        sEmojisMap.put(0x1f4c5, 707);
        sEmojisMap.put(0x1f4c6, 708);
        sEmojisMap.put(0x1f4c7, 709);
        sEmojisMap.put(0x1f4c1, 710);
        sEmojisMap.put(0x1f4c2, 711);
        sEmojisMap.put(0x2702, 712);
        sEmojisMap.put(0x1f4cc, 713);
        sEmojisMap.put(0x1f4ce, 714);
        sEmojisMap.put(0x2712, 715);
        sEmojisMap.put(0x270f, 716);
        sEmojisMap.put(0x1f4cf, 717);
        sEmojisMap.put(0x1f4d0, 718);
        sEmojisMap.put(0x1f4d5, 719);
        sEmojisMap.put(0x1f4d7, 720);
        sEmojisMap.put(0x1f4d8, 721);
        sEmojisMap.put(0x1f4d9, 722);
        sEmojisMap.put(0x1f4d3, 723);
        sEmojisMap.put(0x1f4d4, 724);
        sEmojisMap.put(0x1f4d2, 725);
        sEmojisMap.put(0x1f4da, 726);
        sEmojisMap.put(0x1f4d6, 727);
        sEmojisMap.put(0x1f516, 728);
        sEmojisMap.put(0x1f4db, 729);
        sEmojisMap.put(0x1f52c, 730);
        sEmojisMap.put(0x1f52d, 731);
        sEmojisMap.put(0x1f4f0, 732);
        sEmojisMap.put(0x1f3a8, 733);
        sEmojisMap.put(0x1f3ac, 734);
        sEmojisMap.put(0x1f3a4, 735);
        sEmojisMap.put(0x1f3a7, 736);
        sEmojisMap.put(0x1f3bc, 737);
        sEmojisMap.put(0x1f3b5, 738);
        sEmojisMap.put(0x1f3b6, 739);
        sEmojisMap.put(0x1f3b9, 740);
        sEmojisMap.put(0x1f3bb, 741);
        sEmojisMap.put(0x1f3ba, 742);
        sEmojisMap.put(0x1f3b7, 743);
        sEmojisMap.put(0x1f3b8, 744);
        sEmojisMap.put(0x1f47e, 745);
        sEmojisMap.put(0x1f3ae, 746);
        sEmojisMap.put(0x1f0cf, 747);
        sEmojisMap.put(0x1f3b4, 748);
        sEmojisMap.put(0x1f004, 749);
        sEmojisMap.put(0x1f3b2, 750);
        sEmojisMap.put(0x1f3af, 751);
        sEmojisMap.put(0x1f3c8, 752);
        sEmojisMap.put(0x1f3c0, 753);
        sEmojisMap.put(0x26bd, 754);
        sEmojisMap.put(0x26be, 755);
        sEmojisMap.put(0x1f3be, 756);
        sEmojisMap.put(0x1f3b1, 757);
        sEmojisMap.put(0x1f3c9, 758);
        sEmojisMap.put(0x1f3b3, 759);
        sEmojisMap.put(0x26f3, 760);
        sEmojisMap.put(0x1f6b5, 761);
        sEmojisMap.put(0x1f6b4, 762);
        sEmojisMap.put(0x1f3c1, 763);
        sEmojisMap.put(0x1f3c7, 764);
        sEmojisMap.put(0x1f3c6, 765);
        sEmojisMap.put(0x1f3bf, 766);
        sEmojisMap.put(0x1f3c2, 767);
        sEmojisMap.put(0x1f3ca, 768);
        sEmojisMap.put(0x1f3c4, 769);
        sEmojisMap.put(0x1f3a3, 770);
        sEmojisMap.put(0x2615, 771);
        sEmojisMap.put(0x1f375, 772);
        sEmojisMap.put(0x1f376, 773);
        sEmojisMap.put(0x1f37c, 774);
        sEmojisMap.put(0x1f37a, 775);
        sEmojisMap.put(0x1f37b, 776);
        sEmojisMap.put(0x1f378, 777);
        sEmojisMap.put(0x1f379, 778);
        sEmojisMap.put(0x1f377, 779);
        sEmojisMap.put(0x1f374, 780);
        sEmojisMap.put(0x1f355, 781);
        sEmojisMap.put(0x1f354, 782);
        sEmojisMap.put(0x1f35f, 783);
        sEmojisMap.put(0x1f357, 784);
        sEmojisMap.put(0x1f356, 785);
        sEmojisMap.put(0x1f35d, 786);
        sEmojisMap.put(0x1f35b, 787);
        sEmojisMap.put(0x1f364, 788);
        sEmojisMap.put(0x1f371, 789);
        sEmojisMap.put(0x1f363, 790);
        sEmojisMap.put(0x1f365, 791);
        sEmojisMap.put(0x1f359, 792);
        sEmojisMap.put(0x1f358, 793);
        sEmojisMap.put(0x1f35a, 794);
        sEmojisMap.put(0x1f35c, 795);
        sEmojisMap.put(0x1f372, 796);
        sEmojisMap.put(0x1f362, 797);
        sEmojisMap.put(0x1f361, 798);
        sEmojisMap.put(0x1f373, 799);
        sEmojisMap.put(0x1f35e, 800);
        sEmojisMap.put(0x1f369, 801);
        sEmojisMap.put(0x1f36e, 802);
        sEmojisMap.put(0x1f366, 803);
        sEmojisMap.put(0x1f368, 804);
        sEmojisMap.put(0x1f367, 805);
        sEmojisMap.put(0x1f382, 806);
        sEmojisMap.put(0x1f370, 807);
        sEmojisMap.put(0x1f36a, 808);
        sEmojisMap.put(0x1f36b, 809);
        sEmojisMap.put(0x1f36c, 810);
        sEmojisMap.put(0x1f36d, 811);
        sEmojisMap.put(0x1f36f, 812);
        sEmojisMap.put(0x1f34e, 813);
        sEmojisMap.put(0x1f34f, 814);
        sEmojisMap.put(0x1f34a, 815);
        sEmojisMap.put(0x1f34b, 816);
        sEmojisMap.put(0x1f352, 817);
        sEmojisMap.put(0x1f347, 818);
        sEmojisMap.put(0x1f349, 819);
        sEmojisMap.put(0x1f353, 820);
        sEmojisMap.put(0x1f351, 821);
        sEmojisMap.put(0x1f348, 822);
        sEmojisMap.put(0x1f34c, 823);
        sEmojisMap.put(0x1f350, 824);
        sEmojisMap.put(0x1f34d, 825);
        sEmojisMap.put(0x1f360, 826);
        sEmojisMap.put(0x1f346, 827);
        sEmojisMap.put(0x1f345, 828);
        sEmojisMap.put(0x1f33d, 829);

        // Places
        sEmojisMap.put(0x1f3e0, 900);
        sEmojisMap.put(0x1f3e1, 901);
        sEmojisMap.put(0x1f3eb, 902);
        sEmojisMap.put(0x1f3e2, 903);
        sEmojisMap.put(0x1f3e3, 904);
        sEmojisMap.put(0x1f3e5, 905);
        sEmojisMap.put(0x1f3e6, 906);
        sEmojisMap.put(0x1f3ea, 907);
        sEmojisMap.put(0x1f3e9, 908);
        sEmojisMap.put(0x1f3e8, 909);
        sEmojisMap.put(0x1f492, 910);
        sEmojisMap.put(0x26ea, 911);
        sEmojisMap.put(0x1f3ec, 912);
        sEmojisMap.put(0x1f3e4, 913);
        sEmojisMap.put(0x1f307, 914);
        sEmojisMap.put(0x1f306, 915);
        sEmojisMap.put(0x1f3ef, 916);
        sEmojisMap.put(0x1f3f0, 917);
        sEmojisMap.put(0x26fa, 918);
        sEmojisMap.put(0x1f3ed, 919);
        sEmojisMap.put(0x1f5fc, 920);
        sEmojisMap.put(0x1f5fe, 921);
        sEmojisMap.put(0x1f5fb, 922);
        sEmojisMap.put(0x1f304, 923);
        sEmojisMap.put(0x1f305, 924);
        sEmojisMap.put(0x1f303, 925);
        sEmojisMap.put(0x1f5fd, 926);
        sEmojisMap.put(0x1f309, 927);
        sEmojisMap.put(0x1f3a0, 928);
        sEmojisMap.put(0x1f3a1, 929);
        sEmojisMap.put(0x26f2, 930);
        sEmojisMap.put(0x1f3a2, 931);
        sEmojisMap.put(0x1f6a2, 932);
        sEmojisMap.put(0x26f5, 933);
        sEmojisMap.put(0x1f6a4, 934);
        sEmojisMap.put(0x1f6a3, 935);
        sEmojisMap.put(0x2693, 936);
        sEmojisMap.put(0x1f680, 937);
        sEmojisMap.put(0x2708, 938);
        sEmojisMap.put(0x1f4ba, 939);
        sEmojisMap.put(0x1f681, 940);
        sEmojisMap.put(0x1f682, 941);
        sEmojisMap.put(0x1f68a, 942);
        sEmojisMap.put(0x1f689, 943);
        sEmojisMap.put(0x1f69e, 944);
        sEmojisMap.put(0x1f686, 945);
        sEmojisMap.put(0x1f684, 946);
        sEmojisMap.put(0x1f685, 947);
        sEmojisMap.put(0x1f688, 948);
        sEmojisMap.put(0x1f687, 949);
        sEmojisMap.put(0x1f69d, 950);
        sEmojisMap.put(0x1f68b, 951); // TODO (rockerhieu) review this emoji
        sEmojisMap.put(0x1f683, 952);
        sEmojisMap.put(0x1f68e, 953);
        sEmojisMap.put(0x1f68c, 954);
        sEmojisMap.put(0x1f68d, 955);
        sEmojisMap.put(0x1f699, 956);
        sEmojisMap.put(0x1f698, 957);
        sEmojisMap.put(0x1f697, 958);
        sEmojisMap.put(0x1f695, 959);
        sEmojisMap.put(0x1f696, 960);
        sEmojisMap.put(0x1f69b, 961);
        sEmojisMap.put(0x1f69a, 962);
        sEmojisMap.put(0x1f6a8, 963);
        sEmojisMap.put(0x1f693, 964);
        sEmojisMap.put(0x1f694, 965);
        sEmojisMap.put(0x1f692, 966);
        sEmojisMap.put(0x1f691, 967);
        sEmojisMap.put(0x1f690, 968);
        sEmojisMap.put(0x1f6b2, 969);
        sEmojisMap.put(0x1f6a1, 970);
        sEmojisMap.put(0x1f69f, 971);
        sEmojisMap.put(0x1f6a0, 972);
        sEmojisMap.put(0x1f69c, 973);
        sEmojisMap.put(0x1f488, 974);
        sEmojisMap.put(0x1f68f, 975);
        sEmojisMap.put(0x1f3ab, 976);
        sEmojisMap.put(0x1f6a6, 977);
        sEmojisMap.put(0x1f6a5, 978);
        sEmojisMap.put(0x26a0, 979);
        sEmojisMap.put(0x1f6a7, 980);
        sEmojisMap.put(0x1f530, 981);
        sEmojisMap.put(0x26fd, 982);
        sEmojisMap.put(0x1f3ee, 983);
        sEmojisMap.put(0x1f3b0, 984);
        sEmojisMap.put(0x2668, 985);
        sEmojisMap.put(0x1f5ff, 986);
        sEmojisMap.put(0x1f3aa, 987);
        sEmojisMap.put(0x1f3ad, 988);
        sEmojisMap.put(0x1f4cd, 989);
        sEmojisMap.put(0x1f6a9, 990);

        // Symbols
        sEmojisMap.put(0x1f51f, 1210);
        sEmojisMap.put(0x1f522, 1211);
        sEmojisMap.put(0x1f523, 1213);
        sEmojisMap.put(0x2b06, 1214);
        sEmojisMap.put(0x2b07, 1215);
        sEmojisMap.put(0x2b05, 1216);
        sEmojisMap.put(0x27a1, 1217);
        sEmojisMap.put(0x1f520, 1218);
        sEmojisMap.put(0x1f521, 1219);
        sEmojisMap.put(0x1f524, 1220);
        sEmojisMap.put(0x2197, 1221);
        sEmojisMap.put(0x2196, 1222);
        sEmojisMap.put(0x2198, 1223);
        sEmojisMap.put(0x2199, 1224);
        sEmojisMap.put(0x2194, 1225);
        sEmojisMap.put(0x2195, 1226);
        sEmojisMap.put(0x1f504, 1227);
        sEmojisMap.put(0x25c0, 1228);
        sEmojisMap.put(0x25b6, 1229);
        sEmojisMap.put(0x1f53c, 1230);
        sEmojisMap.put(0x1f53d, 1231);
        sEmojisMap.put(0x21a9, 1232);
        sEmojisMap.put(0x21aa, 1233);
        sEmojisMap.put(0x2139, 1234);
        sEmojisMap.put(0x23ea, 1235);
        sEmojisMap.put(0x23e9, 1236);
        sEmojisMap.put(0x23eb, 1237);
        sEmojisMap.put(0x23ec, 1238);
        sEmojisMap.put(0x2935, 1239);
        sEmojisMap.put(0x2934, 1240);
        sEmojisMap.put(0x1f197, 1241);
        sEmojisMap.put(0x1f500, 1242);
        sEmojisMap.put(0x1f501, 1243);
        sEmojisMap.put(0x1f502, 1244);
        sEmojisMap.put(0x1f195, 1245);
        sEmojisMap.put(0x1f199, 1246);
        sEmojisMap.put(0x1f192, 1247);
        sEmojisMap.put(0x1f193, 1248);
        sEmojisMap.put(0x1f196, 1249);
        sEmojisMap.put(0x1f4f6, 1250);
        sEmojisMap.put(0x1f3a6, 1251);
        sEmojisMap.put(0x1f201, 1252);
        sEmojisMap.put(0x1f22f, 1253);
        sEmojisMap.put(0x1f233, 1254);
        sEmojisMap.put(0x1f235, 1255);
        sEmojisMap.put(0x1f234, 1256);
        sEmojisMap.put(0x1f232, 1257);
        sEmojisMap.put(0x1f250, 1258);
        sEmojisMap.put(0x1f239, 1259);
        sEmojisMap.put(0x1f23a, 1260);
        sEmojisMap.put(0x1f236, 1261);
        sEmojisMap.put(0x1f21a, 1262);
        sEmojisMap.put(0x1f6bb, 1263);
        sEmojisMap.put(0x1f6b9, 1264);
        sEmojisMap.put(0x1f6ba, 1265);
        sEmojisMap.put(0x1f6bc, 1266);
        sEmojisMap.put(0x1f6be, 1267);
        sEmojisMap.put(0x1f6b0, 1268);
        sEmojisMap.put(0x1f6ae, 1269);
        sEmojisMap.put(0x1f17f, 1270);
        sEmojisMap.put(0x267f, 1271);
        sEmojisMap.put(0x1f6ad, 1272);
        sEmojisMap.put(0x1f237, 1273);
        sEmojisMap.put(0x1f238, 1274);
        sEmojisMap.put(0x1f202, 1275);
        sEmojisMap.put(0x24c2, 1276);
        sEmojisMap.put(0x1f6c2, 1277);
        sEmojisMap.put(0x1f6c4, 1278);
        sEmojisMap.put(0x1f6c5, 1279);
        sEmojisMap.put(0x1f6c3, 1280);
        sEmojisMap.put(0x1f251, 1281);
        sEmojisMap.put(0x3299, 1282);
        sEmojisMap.put(0x3297, 1283);
        sEmojisMap.put(0x1f191, 1284);
        sEmojisMap.put(0x1f198, 1285);
        sEmojisMap.put(0x1f194, 1286);
        sEmojisMap.put(0x1f6ab, 1287);
        sEmojisMap.put(0x1f51e, 1288);
        sEmojisMap.put(0x1f4f5, 1289);
        sEmojisMap.put(0x1f6af, 1290);
        sEmojisMap.put(0x1f6b1, 1291);
        sEmojisMap.put(0x1f6b3, 1292);
        sEmojisMap.put(0x1f6b7, 1293);
        sEmojisMap.put(0x1f6b8, 1294);
        sEmojisMap.put(0x26d4, 1295);
        sEmojisMap.put(0x2733, 1296);
        sEmojisMap.put(0x2747, 1297);
        sEmojisMap.put(0x274e, 1298);
        sEmojisMap.put(0x2705, 1299);
        sEmojisMap.put(0x2734, 1300);
        sEmojisMap.put(0x1f49f, 1301);
        sEmojisMap.put(0x1f19a, 1302);
        sEmojisMap.put(0x1f4f3, 1303);
        sEmojisMap.put(0x1f4f4, 1304);
        sEmojisMap.put(0x1f170, 1305);
        sEmojisMap.put(0x1f171, 1306);
        sEmojisMap.put(0x1f18e, 1307);
        sEmojisMap.put(0x1f17e, 1308);
        sEmojisMap.put(0x1f4a0, 1309);
        sEmojisMap.put(0x27bf, 1310);
        sEmojisMap.put(0x267b, 1311);
        sEmojisMap.put(0x2648, 1312);
        sEmojisMap.put(0x2649, 1313);
        sEmojisMap.put(0x264a, 1314);
        sEmojisMap.put(0x264b, 1315);
        sEmojisMap.put(0x264c, 1316);
        sEmojisMap.put(0x264d, 1317);
        sEmojisMap.put(0x264e, 1318);
        sEmojisMap.put(0x264f, 1319);
        sEmojisMap.put(0x2650, 1320);
        sEmojisMap.put(0x2651, 1321);
        sEmojisMap.put(0x2652, 1322);
        sEmojisMap.put(0x2653, 1323);
        sEmojisMap.put(0x26ce, 1324);
        sEmojisMap.put(0x1f52f, 1325);
        sEmojisMap.put(0x1f3e7, 1326);
        sEmojisMap.put(0x1f4b9, 1327);
        sEmojisMap.put(0x1f4b2, 1328);
        sEmojisMap.put(0x1f4b1, 1329);


        sEmojisMap.put(0x00a9, 1330);
        sEmojisMap.put(0x00ae, 1331);


        sEmojisMap.put(0x2122, 1332);
        sEmojisMap.put(0x274c, 1333);
        sEmojisMap.put(0x203c, 1334);
        sEmojisMap.put(0x2049, 1335);
        sEmojisMap.put(0x2757, 1336);
        sEmojisMap.put(0x2753, 1337);
        sEmojisMap.put(0x2755, 1338);
        sEmojisMap.put(0x2754, 1339);
        sEmojisMap.put(0x2b55, 1340);
        sEmojisMap.put(0x1f51d, 1341);
        sEmojisMap.put(0x1f51a, 1342);
        sEmojisMap.put(0x1f519, 1343);
        sEmojisMap.put(0x1f51b, 1344);
        sEmojisMap.put(0x1f51c, 1345);
        sEmojisMap.put(0x1f503, 1346);
        sEmojisMap.put(0x1f55b, 1347);
        sEmojisMap.put(0x1f567, 1348);
        sEmojisMap.put(0x1f550, 1349);
        sEmojisMap.put(0x1f55c, 1350);
        sEmojisMap.put(0x1f551, 1351);
        sEmojisMap.put(0x1f55d, 1352);
        sEmojisMap.put(0x1f552, 1353);
        sEmojisMap.put(0x1f55e, 1354);
        sEmojisMap.put(0x1f553, 1355);
        sEmojisMap.put(0x1f55f, 1356);
        sEmojisMap.put(0x1f554, 1357);
        sEmojisMap.put(0x1f560, 1358);
        sEmojisMap.put(0x1f555, 1359);
        sEmojisMap.put(0x1f556, 1360);
        sEmojisMap.put(0x1f557, 1361);
        sEmojisMap.put(0x1f558, 1362);
        sEmojisMap.put(0x1f559, 1363);
        sEmojisMap.put(0x1f55a, 1364);
        sEmojisMap.put(0x1f561, 1365);
        sEmojisMap.put(0x1f562, 1366);
        sEmojisMap.put(0x1f563, 1367);
        sEmojisMap.put(0x1f564, 1368);
        sEmojisMap.put(0x1f565, 1369);
        sEmojisMap.put(0x1f566, 1370);
        sEmojisMap.put(0x2716, 1371);
        sEmojisMap.put(0x2795, 1372);
        sEmojisMap.put(0x2796, 1373);
        sEmojisMap.put(0x2797, 1374);
        sEmojisMap.put(0x2660, 1375);
        sEmojisMap.put(0x2665, 1376);
        sEmojisMap.put(0x2663, 1377);
        sEmojisMap.put(0x2666, 1378);
        sEmojisMap.put(0x1f4ae, 1379);
        sEmojisMap.put(0x1f4af, 1380);
        sEmojisMap.put(0x2714, 1381);
        sEmojisMap.put(0x2611, 1382);
        sEmojisMap.put(0x1f518, 1383);
        sEmojisMap.put(0x1f517, 1384);
        sEmojisMap.put(0x27b0, 1385);
        sEmojisMap.put(0x3030, 1386);
        sEmojisMap.put(0x303d, 1387);
        sEmojisMap.put(0x1f531, 1388);
        sEmojisMap.put(0x25fc, 1389);
        sEmojisMap.put(0x25fb, 1390);
        sEmojisMap.put(0x25fe, 1391);
        sEmojisMap.put(0x25fd, 1392);
        sEmojisMap.put(0x25aa, 1393);
        sEmojisMap.put(0x25ab, 1394);
        sEmojisMap.put(0x1f53a, 1395);
        sEmojisMap.put(0x1f532, 1396);
        sEmojisMap.put(0x1f533, 1397);
        sEmojisMap.put(0x26ab, 1398);
        sEmojisMap.put(0x26aa, 1399);
        sEmojisMap.put(0x1f534, 1400);
        sEmojisMap.put(0x1f535, 1401);
        sEmojisMap.put(0x1f53b, 1402);
        sEmojisMap.put(0x2b1c, 1403);
        sEmojisMap.put(0x2b1b, 1404);
        sEmojisMap.put(0x1f536, 1405);
        sEmojisMap.put(0x1f537, 1406);
        sEmojisMap.put(0x1f538, 1407);
        sEmojisMap.put(0x1f539, 1408);
    }


    public static SparseIntArray getSEmojisMap() {
        return sEmojisMap;
    }

    public static int getEmojiResource(int codePoint) {
        return sEmojisMap.get(codePoint);
    }

    public static void addEmojis(Context context, Spannable text, int emojiSize, boolean isEditText) {
        addEmojis(context, text, emojiSize, 0, -1, isEditText);
    }

    public static int getEmojiPosition(String text) {
        int textLength = text.length();
        int skip;

        for (int i = 0; i < textLength; i += skip) {
            int pos = 0;
            int unicode = Character.codePointAt(text, i);
            skip = Character.charCount(unicode);

            if (unicode > 0xff || unicode == 0x00a9 || unicode == 0x00ae) {
                pos = getEmojiResource(unicode);
            }

            if (pos == 0 && i + skip < textLength) {
                int followUnicode = Character.codePointAt(text, i + skip);
                if (followUnicode == 0x20e3) {
                    int followSkip = Character.charCount(followUnicode);
                    switch (unicode) {
                        case 0x0031:
                            pos = 1200;
                            break;
                        case 0x0032:
                            pos = 1201;
                            break;
                        case 0x0033:
                            pos = 1202;
                            break;
                        case 0x0034:
                            pos = 1203;
                            break;
                        case 0x0035:
                            pos = 1204;
                            break;
                        case 0x0036:
                            pos = 1205;
                            break;
                        case 0x0037:
                            pos = 1206;
                            break;
                        case 0x0038:
                            pos = 1207;
                            break;
                        case 0x0039:
                            pos = 1208;
                            break;
                        case 0x0030:
                            pos = 1209;
                            break;
                        case 0x0023:
                            pos = 1212;
                            break;
                        default:
                            followSkip = 0;
                            break;
                    }
                    skip += followSkip;
                } else {
                    int followSkip = Character.charCount(followUnicode);
                    switch (unicode) {
                        case 0x1f1ef:
                            pos = (followUnicode == 0x1f1f5) ? 991 : 0;
                            break;
                        case 0x1f1fa:
                            pos = (followUnicode == 0x1f1f8) ? 995 : 0;
                            break;
                        case 0x1f1eb:
                            pos = (followUnicode == 0x1f1f7) ? 996 : 0;
                            break;
                        case 0x1f1e9:
                            pos = (followUnicode == 0x1f1ea) ? 993 : 0;
                            break;
                        case 0x1f1ee:
                            pos = (followUnicode == 0x1f1f9) ? 998 : 0;
                            break;
                        case 0x1f1ec:
                            pos = (followUnicode == 0x1f1e7) ? 1000 : 0;
                            break;
                        case 0x1f1ea:
                            pos = (followUnicode == 0x1f1f8) ? 997 : 0;
                            break;
                        case 0x1f1f7:
                            pos = (followUnicode == 0x1f1fa) ? 999 : 0;
                            break;
                        case 0x1f1e8:
                            pos = (followUnicode == 0x1f1f3) ? 994 : 0;
                            break;
                        case 0x1f1f0:
                            pos = (followUnicode == 0x1f1f7) ? 992 : 0;
                            break;
                        default:
                            followSkip = 0;
                            break;
                    }
                    skip += followSkip;
                }
            }

            if (pos > 0) {
                return pos;
            }
        }
        return 0;
    }

    private static final int MAX_EMOJICON_COUNT = 50;

    public static void addEmojis(Context context, Spannable text, int emojiSize, int index, int length, boolean isEditText) {
        int textLength = text.length();
        int textLengthToProcessMax = textLength - index;
        int textLengthToProcess = length < 0 || length >= textLengthToProcessMax ? textLength : (length + index);

        // remove spans throughout all text
        int counter = 0;
        EmojiconSpan[] oldSpans = text.getSpans(0, textLength, EmojiconSpan.class);

        if (isEditText) {
            if (oldSpans.length > MAX_EMOJICON_COUNT) {
                return;
            }
            for (int i = 0; i < oldSpans.length; i++) {
                text.removeSpan(oldSpans[i]);
            }
        }


        int skip;
        counter = 0;

        for (int i = index; i < textLengthToProcess; i += skip) {
            int icon = 0;
            int unicode = Character.codePointAt(text, i);
            skip = Character.charCount(unicode);

            if (unicode > 0xff || unicode == 0x00a9 || unicode == 0x00ae) {
                icon = getEmojiResource(unicode);
            }

            if (icon == 0 && i + skip < textLengthToProcess) {
                int followUnicode = Character.codePointAt(text, i + skip);
                if (followUnicode == 0x20e3) {
                    int followSkip = Character.charCount(followUnicode);
                    switch (unicode) {
                        case 0x0031:
                            icon = 1200;
                            break;
                        case 0x0032:
                            icon = 1201;
                            break;
                        case 0x0033:
                            icon = 1202;
                            break;
                        case 0x0034:
                            icon = 1203;
                            break;
                        case 0x0035:
                            icon = 1204;
                            break;
                        case 0x0036:
                            icon = 1205;
                            break;
                        case 0x0037:
                            icon = 1206;
                            break;
                        case 0x0038:
                            icon = 1207;
                            break;
                        case 0x0039:
                            icon = 1208;
                            break;
                        case 0x0030:
                            icon = 1209;
                            break;
                        case 0x0023:
                            icon = 1212;
                            break;
                        default:
                            followSkip = 0;
                            break;
                    }
                    skip += followSkip;
                } else {
                    int followSkip = Character.charCount(followUnicode);
                    switch (unicode) {
                        case 0x1f1ef:
                            icon = (followUnicode == 0x1f1f5) ? 991 : 0;
                            break;
                        case 0x1f1fa:
                            icon = (followUnicode == 0x1f1f8) ? 995 : 0;
                            break;
                        case 0x1f1eb:
                            icon = (followUnicode == 0x1f1f7) ? 996 : 0;
                            break;
                        case 0x1f1e9:
                            icon = (followUnicode == 0x1f1ea) ? 993 : 0;
                            break;
                        case 0x1f1ee:
                            icon = (followUnicode == 0x1f1f9) ? 998 : 0;
                            break;
                        case 0x1f1ec:
                            icon = (followUnicode == 0x1f1e7) ? 1000 : 0;
                            break;
                        case 0x1f1ea:
                            icon = (followUnicode == 0x1f1f8) ? 997 : 0;
                            break;
                        case 0x1f1f7:
                            icon = (followUnicode == 0x1f1fa) ? 999 : 0;
                            break;
                        case 0x1f1e8:
                            icon = (followUnicode == 0x1f1f3) ? 994 : 0;
                            break;
                        case 0x1f1f0:
                            icon = (followUnicode == 0x1f1f7) ? 992 : 0;
                            break;
                        default:
                            followSkip = 0;
                            break;
                    }
                    skip += followSkip;
                }
            }

            if (icon > 0) {
                counter++;
                if (counter > MAX_EMOJICON_COUNT) {
                    break;
                }

                text.setSpan(new EmojiconSpan(context.getResources(), icon, emojiSize), i, i + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
