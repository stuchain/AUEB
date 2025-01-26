from score import score
from max import maxer
def medium(players,way):
    import random
    #################################################from score import *
    # dhmiourgw mia lista pou anaparista ta stoixeia tou eukolou epipedou
    medium_list = ['10'+u'\u2660', '10'+u'\u2665','10'+u'\u2663', '10'+u'\u2666','9'+u'\u2660', '9'+u'\u2665','9'+u'\u2663', '9'+u'\u2666',
                   '8'+u'\u2660', '8'+u'\u2665','8'+u'\u2663', '8'+u'\u2666','7'+u'\u2660', '7'+u'\u2665','7'+u'\u2663', '7'+u'\u2666',
                   '6'+u'\u2660', '6'+u'\u2665','6'+u'\u2663', '6'+u'\u2666','5'+u'\u2660', '5'+u'\u2665','5'+u'\u2663', '5'+u'\u2666',
                   '4'+u'\u2660', '4'+u'\u2665','4'+u'\u2663', '4'+u'\u2666','3'+u'\u2660', '3'+u'\u2665','3'+u'\u2663', '3'+u'\u2666',
                   '2'+u'\u2660', '2'+u'\u2665','2'+u'\u2663', '2'+u'\u2666','A'+u'\u2660', 'A'+u'\u2665','A'+u'\u2663', 'A'+u'\u2666']
    # dhmiourgw mia lista me ta score twn paiktwn
    score_list = [0]* players
    random.shuffle(medium_list)    # thn kanw random
    # spaw thn arxikh lista se 4 listes gia na mporesw na tis anaparasthsw san pinaka sth sunexeia
    l1 = [medium_list[0],medium_list[1],medium_list[2],medium_list[3],medium_list[4],medium_list[5],medium_list[6],medium_list[7],medium_list[8],medium_list[9]]
    l2 = [medium_list[10],medium_list[11],medium_list[12],medium_list[13],medium_list[14],medium_list[15],medium_list[16],medium_list[17],medium_list[18],medium_list[19]]
    l3 = [medium_list[20],medium_list[21],medium_list[22],medium_list[23],medium_list[24],medium_list[25],medium_list[26],medium_list[27],medium_list[28],medium_list[29]]
    l4 = [medium_list[30],medium_list[31],medium_list[32],medium_list[33],medium_list[34],medium_list[35],medium_list[36],medium_list[37],medium_list[38],medium_list[39]]
    l = [l1, l2, l3, l4]
    # ftiaxnw nees listes tis opoies tha tropopoiw kathe fora analoga me tis kartes pou epilegei o xrhsths
    x1 = ['X']*10
    x2 = ['X']*10
    x3 = ['X']*10
    x4 = ['X']*10
    x = [x1, x2, x3, x4]
    # emfanizw ton pinaka
    print('\t', '1', '\t', '2', '\t', '3', '\t', '4','\t', '5', '\t', '6', '\t', '7', '\t', '8','\t', '9', '\t', '10')
    for z in range(4):
        print(str(z+1), '\t', x[z][0], '\t', x[z][1], '\t', x[z][2], '\t', x[z][3],'\t', x[z][4], '\t', x[z][5], '\t', x[z][6], '\t', x[z][7], '\t', x[z][8], '\t', x[z][9])
    w = 1    # metrhths twn anoiktwn kartwn
    # h ekswterikh while elegxei an exei teleiwsei to paixnidi
    while w <= 40:
        i = 1
        while i <= players and w<=40:
            for j in range (2): # epanalhpsh gia tis 2 kartes          
                if w <=40:
                    if j==0:
                        a = 'Παίκτη '+str(i)+' : Δώσε γραμμή και στήλη πρώτης κάρτας:'
                    else:
                        a = 'Παίκτη ' + str(i)+' : Δώσε γραμμή και στήλη δεύτερης κάρτας:'
                    grammes, stiles = [int(x)for x in input(a).split()]
                    # elegxos egkurothtas
                    while (grammes > 4 or grammes < 1) or (stiles > 10 or stiles < 1):
                        print('Δώστε έγκυρο αριθμό γραμμής και στήλης.')
                        grammes, stiles = [int(x)for x in input(a).split()]
                    # elegxos egkurothtas
                    while x[grammes-1][stiles-1] != 'X':
                        print('Η κάρτα είναι ήδη ανοικτή, δοκιμάστε ξανά')
                        grammes, stiles = [int(x)for x in input(a).split()]
                        # elegxos egkurothtas
                        while (grammes > 4 or grammes < 1) or (stiles > 10 or stiles < 1):
                            print('Δώστε έγκυρο αριθμό γραμμής και στήλης.')
                            grammes, stiles = [int(x)for x in input(a).split()]
                # anoigw th karta
                x[grammes-1][stiles-1] = l[grammes-1][stiles-1]
                if j==0:
                    card1 = x[grammes-1][stiles-1]
                    gr1, st1 = grammes, stiles # krataw th thesh ths kartas
                else:
                    card2 = x[grammes-1][stiles-1]
                    gr2, st2 = grammes, stiles # krataw th thesh ths kartas   
                # emfanizw ton pinaka
                print('\t', '1', '\t', '2', '\t', '3', '\t', '4','\t', '5', '\t', '6', '\t', '7', '\t', '8','\t', '9', '\t', '10')
                for z in range(4):
                    print(str(z+1), '\t', x[z][0], '\t', x[z][1], '\t', x[z][2], '\t', x[z][3],'\t', x[z][4], '\t', x[z][5], '\t', x[z][6], '\t', x[z][7], '\t', x[z][8], '\t', x[z][9])
                w += 1
                if w>40:
                    break
            if card1[0] == card2[0]:
                score_list[i-1] = score_list[i-1] + score(card1,card2,way)
                print('Επιτυχές ταίριασμα +', score(card1,card2,way), ' πόντοι! Παίκτη',i, 'έχεις συνολικά', score_list[i-1], ' πόντους.')
            i += 1

    return(maxer(score_list,players))

