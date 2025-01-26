from score import score
from max import maxer
def easy1(players,way):
    import random
    # dhmiourgw mia lista pou anaparista ta stoixeia tou eukolou epipedou
    easy_list = ['10'+u'\u2660', '10'+u'\u2665','10'+u'\u2663', '10'+u'\u2666','Q'+u'\u2660', 'Q'+u'\u2665','Q'+u'\u2663', 'Q'+u'\u2666',
                 'K'+u'\u2660', 'K'+u'\u2665','K'+u'\u2663', 'K'+u'\u2666','J'+u'\u2660', 'J'+u'\u2665','J'+u'\u2663', 'J'+u'\u2666']
    # dhmiourgw mia lista me ta score twn paiktwn
    score_list = [0]* players
    # thn kanw random
    random.shuffle(easy_list)
    # spaw thn arxikh lista se 4 listes gia na mporesw na tis anaparasthsw san pinaka sth sunexeia
    l1 = [easy_list[0], easy_list[1], easy_list[2], easy_list[3]]
    l2 = [easy_list[4], easy_list[5], easy_list[6], easy_list[7]]
    l3 = [easy_list[8], easy_list[9], easy_list[10], easy_list[11]]
    l4 = [easy_list[12], easy_list[13], easy_list[14], easy_list[15]]
    l = [l1, l2, l3, l4]
    # ftiaxnw nees listes tis opoies tha tropopoiw kathe fora analoga me tis kartes pou epilegei o xrhsths
    x1 = ['X']*4
    x2 = ['X']*4
    x3 = ['X']*4
    x4 = ['X']*4
    x = [x1, x2, x3, x4]
    # emfanizw ton pinaka
    print('\t', '1', '\t', '2', '\t', '3', '\t', '4')
    for z in range(4):
        print(str(z+1), '\t', x[z][0], '\t', x[z][1], '\t', x[z][2], '\t', x[z][3])
    f = False     # thewrw th metavlhth f 'diakopth' pou xreiazomai sth periptwsh eidikhs kartas J
    f1 = False     # thewrw th metavlhth f 'diakopth' pou xreiazomai sth periptwsh eidikhs kartas K
    y = 0   # tha xrisimopoihtai stin periptwsh J
    w = 1    # metrhths twn anoiktwn kartwn
    # h ekswterikh while elegxei an exei teleiwsei to paixnidi
    while w <= 16:
        i = 1 # metrhths paiktwn 
        while i <= players and w<=16:
            f4 = False #diakoptis gia eidikh periptwsh q kai k an vrei symvola twn q kai k idia
            if f == True:
                i = y   # ksanapaizei o paikths
                f = False
            if f1 == True:
                f1 = False
                if i == players:
                    i = 1   # an xanei th seira tou o teleftaios paikths, tote paizei o prwtos. gia afto to i==1
                else:
                    i = i+1   # xanei th seira tou o paikths
            for j in range (2): # epanalhpsh gia tis 2 kartes       
                if w <=16:
                    if j==0:
                        a = 'Παίκτη '+str(i)+' : Δώσε γραμμή και στήλη πρώτης κάρτας:'
                    else:
                        a = 'Παίκτη ' + str(i)+' : Δώσε γραμμή και στήλη δεύτερης κάρτας:'
                    grammes, stiles = [int(x)for x in input(a).split()]
                    # elegxos egkurothtas
                    while (grammes > 4 or grammes < 1) or (stiles > 4 or stiles < 1):
                        print('Δώστε έγκυρο αριθμό γραμμής και στήλης.')
                        grammes, stiles = [int(x)for x in input(a).split()]
                    # elegxos egkurothtas
                    while x[grammes-1][stiles-1] != 'X':
                        print('Η κάρτα είναι ήδη ανοικτή, δοκιμάστε ξανά')
                        grammes, stiles = [int(x)for x in input(a).split()]
                        # elegxos egkurothtas
                        while (grammes > 4 or grammes < 1) or (stiles > 4 or stiles < 1):
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
                print('\t', '1', '\t', '2', '\t', '3', '\t', '4')
                for z in range(4):
                    print(str(z+1), '\t', x[z][0], '\t', x[z][1], '\t', x[z][2], '\t', x[z][3])
                w+=1    
                if w>16:
                    break    
            if (card1[0] == card2[0])or(card1[0]=='1' and card1[2]==card2[1])or(card2[0]=='1' and card2[2]==card1[1])or(card1[1]==card2[1]):
                score_list[i-1] = score_list[i-1] + score(card1,card2,way)
                print('Επιτυχές ταίριασμα +', score(card1,card2,way), ' πόντοι! Παίκτη',i, 'έχεις συνολικά', score_list[i-1], ' πόντους.')
                # eidikh periptwsh kartas J
                if card1[0] == 'J' and card2[0] == 'J' and w<=16:
                    print('Άνοιξες ειδική κάρτα J, παίκτη', i, ' ξανά παίξε.')
                    f = True
                    y=i
                # eidikh periptwsh kartas K
                if card1[0] == 'K' and card2[0] == 'K' and w<=16:
                    f1 = True
                    if i==players:
                            i=0
                    print('Άνοιξες ειδική κάρτα K, παίκτη ',i+1,' χάνεις την σειρά σου.')
            # eidikh periptwsh kartwn Q kai K
            if (card1[0] == 'Q' and card2[0] == 'K') or (card2[0] == 'Q' and card1[0] == 'K') and w<=16:
                if (card1[1]==card2[1]):
                    f4=True
                w+=1
                if w>16:
                    break
                a = 'Παίκτη '+str(i)+' : Δώσε γραμμή και στήλη τρίτης κάρτας:'
                grammes, stiles = [int(x)for x in input(a).split()]
                # elegxos egkurothtas
                while (grammes > 4 or grammes < 1) or (stiles > 4 or stiles < 1):
                    print('Δώστε έγκυρο αριθμό γραμμής και στήλης.')
                    grammes, stiles = [int(x)for x in input(a).split()]
                # elegxos egkurothtas
                while x[grammes-1][stiles-1] != 'X':
                    print('Η κάρτα είναι ήδη ανοικτή, δοκιμάστε ξανά')
                    grammes, stiles = [int(x)for x in input(a).split()]
                while (grammes > 4 or grammes < 1) or (stiles > 4 or stiles < 1):
                    print('Δώστε έγκυρο αριθμό γραμμής και στήλης.')
                    grammes, stiles = [int(x)for x in input(a).split()]
                # anoigw thn trith karta
                x[grammes-1][stiles-1] = l[grammes-1][stiles-1]
                card3 = x[grammes-1][stiles-1]
                print('\t', '1', '\t', '2', '\t', '3', '\t', '4')
                for z in range(4):
                    print(str(z+1), '\t', x[z][0], '\t', x[z][1], '\t', x[z][2], '\t', x[z][3])
                if ((card1[0] == card3[0]) or (card3[0]=='1'and(card1[1]==card3[2]))or(card3[1]==card1[1]))and f4==False:
                    x[gr2-1][st2-1] = 'X'
                    w = w - 1
                elif ((card2[0] == card3[0]) and (card3[0]=='1'and(card2[1]==card3[2]))or(card3[1]==card2[1]))and f4==False:
                    x[gr1-1][st1-1] = 'X'
                    w = w - 1
                elif f4==False:
                    x[grammes-1][stiles-1] = 'X'
                    x[gr2-1][st2-1] = 'X'
                    x[gr1-1][st1-1] = 'X'
                    w = w - 3
                if (card1[0] == card3[0] or card2[0] == card3[0])or(card3[0]!='1'and((card1[1]==card3[1])or(card3[1]==card2[1])))or(card3[0]=='1'and((card1[1]==card3[2])or(card3[2]==card2[1]))):
                    if card1[0]==card3[0]or card1[1]==card3[1]or(card3[0]=='1'and( card1[1]==card3[2])):
                        card2=card3
                    elif card2[0]==card3[0]or card2[1]==card3[1]or(card3[0]=='1'and( card2[1]==card3[2] or card1[1]==card3[2])):
                        card1=card3
                    score_list[i-1] += score(card1,card2,way)
                    print('Επιτυχές ταίριασμα +', score(card1,card2,way), ' πόντοι! Παίκτη',i, 'έχεις συνολικά', score_list[i-1], ' πόντους.')
               
            i += 1

    return(maxer(score_list,players))
