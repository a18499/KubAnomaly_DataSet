import tensorflow as tf
from sklearn.svm import LinearSVC, SVC
from tensorflow.keras.optimizers import Adam
import numpy as np
from sklearn.model_selection import train_test_split
import time
import sys
import getopt
import matplotlib.pyplot as plt
from sklearn.decomposition import PCA

import csv
from sklearn.preprocessing import StandardScaler, Normalizer
from sklearn.metrics import roc_curve, auc, precision_recall_fscore_support





# exit()


class Dataset():
    def __init__(self, path=None, Normalize=False, Normalize_method='std'):
        if path == None:
            print('Path cannot be None')
            exit()
        print(path)
        # Load data

        self.X = []
        self.Y = []

        with open(path, 'rt') as csvfile:
            csv_reader = csv.reader(csvfile)
            # csv_reader = csv.DictReader(csvfile)
            for idx, row in enumerate(csv_reader):
                if idx == 0:
                    continue
                self.X.append([float(v) for v in row[1:37]])
                # self.X.append([float(v) for v in [row[16],row[25], row[34], row[26], row[13], row[17], row[23], row[19], row[5], row[31], row[21], row[28], 0, row[2], row[33], row[36] ]])
                self.Y.append([int(v) for v in row[37:]].index(1))
        if Normalize:
            self.X = np.array(self.X)

            if Normalize_method == 'std':
                normalizer = StandardScaler()
            elif Normalize_method == 'l2':
                normalizer = Normalizer()

            self.X = normalizer.fit_transform(self.X)
            self.Y = np.array(self.Y)
        else:
            self.X = np.array(self.X)
            self.Y = np.array(self.Y)

class KubAnomaly_Model():
    train_X = []
    train_Y = []
    cnn_flag = False
    #Complex data set
    data_path = ['./Data/cmdinjection.csv', './Data/DVWA_Normal.csv', './Data/DVWA_SQLInjection1.csv',
                 './Data/DVWA_SQLInjection2.csv',
                 './Data/DVWA_SQLInjection3.csv', './Data/sqlinject.csv',
                 './Data/wordPressNormalandAttack/NormalV1.1.csv', './Data/SqlandCommand/AttackV1.1.csv',
                 './Data/SqlandCommand/InsiderSql.csv', './Data/SqlandCommand/NormalV1.2.csv',
                 './Data/brutforce/AttackV1.1.csv', './Data/brutforce/InsiderV1.1.csv']
    # Simple data set
    # data_path = ['./Data/DVWA_Normal.csv', './Data/DVWA_SQLInjection1.csv', './Data/DVWA_SQLInjection2.csv',
    #             './Data/DVWA_SQLInjection3.csv']
    # './Data/wordPressNormalandAttack/NormalV1.1.csv'
    for path in data_path:
        ds = Dataset(path, Normalize=True, Normalize_method='l2')
        train_X.append(ds.X)
        train_Y.append(ds.Y)
        print(ds.X.shape)
    # print(ds.Y.shape)
    train_X = np.concatenate(train_X)
    train_Y = np.concatenate(train_Y)
    print(train_Y.shape)

    X_train, X_test, Y_train, Y_test = train_test_split(train_X, train_Y, test_size=0.2, random_state=1)
    y_GT = Y_test
    model = tf.keras.models.Sequential()


    def generate_plot_figure(self):
        # this file is used to generate plots for paper


        liSVM_fpr = [0.       ,  0.18626645 ,1.        ]
        liSVM_tpr = [0.       ,  0.95863115 ,1.        ]


        rbfSVM_fpr = [0.   ,  0.3125 ,1.    ]
        rbfSVM_tpr = [0.  ,      0.96902751, 1.        ]


        MLP_fpr = [0.        , 0.10567434 ,1.        ]
        MLP_tpr = [0.        , 0.99978341 ,1.        ]


        CNN_fpr = [0.      ,   0.12417763 ,1.        ]
        CNN_tpr = [0.       ,  0.99870045 ,1.        ]


        liSVM_fpr = [0.      ,   0.25164474 ,1.        ]
        liSVM_tpr = [0.       ,  0.97184319 ,1.        ]


        rbfSVM_fpr = [0.       ,  0.24506579 ,1.        ]
        rbfSVM_tpr = [0.       ,  0.97292614 ,1.        ]


        MLP_fpr = [0.      ,   0.12088816 ,1.        ]
        MLP_tpr = [0.      ,   0.99241932 ,1.        ]

        MLP_fpr = [0.      ,   0.11595395 ,1.        ]
        MLP_tpr = [0.      ,   0.96816114 ,1.        ]

        CNN_fpr = [0.      ,   0.13569079 ,1.       ]
        CNN_tpr = [0.     ,    0.98982023 ,1.        ]


        plt.figure()
        lw = 1
        plt.plot(liSVM_fpr, liSVM_tpr,lw=2, label='Linear SVM ROC curve (area = 0.86)')
        plt.plot(rbfSVM_fpr, rbfSVM_tpr,lw=2, label='RBF SVM ROC curve (area = 0.8639)',color='b')
        plt.plot(MLP_fpr, MLP_tpr,lw=2, label='KubAnomaly ROC curve (area = 0.9357)')
        plt.plot(CNN_fpr, CNN_tpr,lw=2, label='CNN ROC curve (area = 0.9270)')
        plt.plot([0, 1], [0, 1], color='navy', lw=lw, linestyle='--')
        plt.xlim([0.0, 1.0])
        plt.ylim([0.0, 1.05])
        plt.xlabel('False Positive Rate')
        plt.ylabel('True Positive Rate')
        plt.title('Receiver operating characteristic')
        plt.legend(loc="lower right")
        plt.show()

    def linear_svm(self):
        # SVM
        print("Linear SVM Testing...")
        #clf = LinearSVC(C=1, random_state=1, max_iter=100000)
        clf = SVC(C=1, random_state=1)
        clf.fit(self.X_train, self.Y_train)
        score_testing = clf.score(self.X_test, self.Y_test)
        y_pred = clf.predict(self.X_test)
        print(score_testing)
        fpr, tpr, thres = roc_curve(self.Y_test, y_pred)
        print("FPR " + str(fpr))
        print("TPR " + str(tpr))
        # print(thres)
        auc_s = auc(fpr, tpr)
        print("ACC " + str(auc_s))


    def kubanomaly(self):
        self.y_GT = self.Y_test
        self.Y_train = tf.keras.utils.to_categorical(self.Y_train, 2)
        self.Y_test = tf.keras.utils.to_categorical(self.Y_test, 2)
        # MLP (KubAnomaly)
        self.model = tf.keras.models.Sequential()
        self.model.add(tf.keras.layers.Dense(64, activation='elu', input_shape=(self.X_train.shape[1],)))
        self.model.add(tf.keras.layers.Dropout(0.4))
        self.model.add(tf.keras.layers.Dense(32, activation='elu'))
        self.model.add(tf.keras.layers.Dropout(0.4))
        self.model.add(tf.keras.layers.Dense(16, activation='elu'))
        self.model.add(tf.keras.layers.Dropout(0.5))

        self.model.add(tf.keras.layers.Dense(2, activation='softmax'))
        self.compile_evaluate()

    def cnn(self):
        #self.y_GT = self.Y_test
        #self.Y_train = tf.keras.utils.to_categorical(self.Y_train, 2)
        #self.Y_test = tf.keras.utils.to_categorical(self.Y_test, 2)
        # CNN
        self.X_train = np.expand_dims(self.X_train,axis=2)
        self.X_test = np.expand_dims(self.X_test,axis=2)

        self.model = tf.keras.models.Sequential()
        self.model.add(tf.keras.layers.Conv1D(16, kernel_size=5, input_shape=(self.X_train.shape[1], 1), padding='same', activation='elu'))
        self.model.add(tf.keras.layers.Conv1D(32, kernel_size=5, padding='same', activation='elu'))
        self.model.add(tf.keras.layers.MaxPooling1D(2))
        self.model.add(tf.keras.layers.Dropout(0.5))
        self.model.add(tf.keras.layers.Flatten())
        self.model.add(tf.keras.layers.Dense(32, activation='elu'))
        self.model.add(tf.keras.layers.Dropout(0.5))
        self.model.add(tf.keras.layers.Dense(16, activation='elu'))
        self.model.add(tf.keras.layers.Dropout(0.5))

        self.model.add(tf.keras.layers.Dense(2, activation='softmax'))
        self.cnn_flag = True
        self.compile_evaluate()

    def compile_evaluate(self):

        self.model.compile(loss=tf.keras.losses.categorical_crossentropy,
                         optimizer=Adam(),
                        metrics=['accuracy'])
        s_time = time.time()
        history = self.model.fit(self.X_train, self.Y_train,
                            batch_size=128,
                            epochs=30,
                            verbose=1,
                            validation_data=(self.X_test, self.Y_test))
        e_time = time.time()
        print('Took {} seconds'.format(e_time - s_time))

        ds = Dataset('./Data/wordPressNormalandAttack/NormalV1.1.csv', Normalize=True, Normalize_method='l2')
        Y = tf.keras.utils.to_categorical(ds.Y, 2)
        if self.cnn_flag == True:
            ds.X = np.expand_dims(ds.X, axis=2)
            #Y = np.expand_dims(Y, axis=2)
            score = self.model.evaluate(ds.X, Y, verbose=1)
        else:
            score = self.model.evaluate(ds.X, Y, verbose=1)
        print(score)


        #y_pred = clf.predict(X_test)
        y_pred = self.model.predict_classes(self.X_test)
        # print y_pred

        fpr, tpr , thres = roc_curve(self.y_GT,y_pred)
        print("FPR " + str(fpr))
        print("TPR " + str(tpr))
        #print(thres)
        auc_s = auc(fpr,tpr)
        print("ACC " + str(auc_s))

        prec, recall, f_s, _ = precision_recall_fscore_support(self.y_GT,y_pred, average='micro')
        print(prec, recall, f_s)

if __name__ == '__main__':
    test = KubAnomaly_Model()
    test.linear_svm()
    test.kubanomaly()
    test.cnn()
    test.generate_plot_figure()
