import tensorflow as tf
from sklearn.svm import LinearSVC, SVC
from tensorflow.keras.optimizers import Adam
import numpy as np
from sklearn.model_selection import train_test_split
import time
from sklearn.decomposition import PCA

import csv
from sklearn.preprocessing import StandardScaler, Normalizer



# this file is used to generate plots for paper


# liSVM_fpr = [0.       ,  0.18626645 ,1.        ]
# liSVM_tpr = [0.       ,  0.95863115 ,1.        ]


# rbfSVM_fpr = [0.   ,  0.3125 ,1.    ]
# rbfSVM_tpr = [0.  ,      0.96902751, 1.        ]


# MLP_fpr = [0.        , 0.10567434 ,1.        ]
# MLP_tpr = [0.        , 0.99978341 ,1.        ]


# CNN_fpr = [0.      ,   0.12417763 ,1.        ]
# CNN_tpr = [0.       ,  0.99870045 ,1.        ]


# liSVM_fpr = [0.      ,   0.25164474 ,1.        ]
# liSVM_tpr = [0.       ,  0.97184319 ,1.        ]


# rbfSVM_fpr = [0.       ,  0.24506579 ,1.        ]
# rbfSVM_tpr = [0.       ,  0.97292614 ,1.        ]


# MLP_fpr = [0.      ,   0.12088816 ,1.        ]
# MLP_tpr = [0.      ,   0.99241932 ,1.        ]


# CNN_fpr = [0.      ,   0.13569079 ,1.       ]
# CNN_tpr = [0.     ,    0.98982023 ,1.        ]


# plt.figure()
# lw = 1
# plt.plot(liSVM_fpr, liSVM_tpr,lw=2, label='Linear SVM ROC curve (area = 0.86)')
# plt.plot(rbfSVM_fpr, rbfSVM_tpr,lw=2, label='RBF SVM ROC curve (area = 0.8639)',color='b')
# plt.plot(MLP_fpr, MLP_tpr,lw=2, label='DAVID ROC curve (area = 0.9357)')
# plt.plot(CNN_fpr, CNN_tpr,lw=2, label='CNN ROC curve (area = 0.9270)')
# plt.plot([0, 1], [0, 1], color='navy', lw=lw, linestyle='--')
# plt.xlim([0.0, 1.0])
# plt.ylim([0.0, 1.05])
# plt.xlabel('False Positive Rate')
# plt.ylabel('True Positive Rate')
# plt.title('Receiver operating characteristic')
# plt.legend(loc="lower right")
# plt.show()


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


train_X = []
train_Y = []

data_path = ['./Data/cmdinjection.csv','./Data/DVWA_Normal.csv','./Data/DVWA_SQLInjection1.csv','./Data/DVWA_SQLInjection2.csv',
 			 './Data/DVWA_SQLInjection3.csv','./Data/sqlinject.csv', './Data/wordPressNormalandAttack/NormalV1.1.csv', './Data/SqlandCommand/AttackV1.1.csv',
 			 './Data/SqlandCommand/InsiderSql.csv', './Data/SqlandCommand/NormalV1.2.csv',
 			 './Data/brutforce/AttackV1.1.csv','./Data/brutforce/InsiderV1.1.csv']

#data_path = ['./Data/DVWA_Normal.csv', './Data/DVWA_SQLInjection1.csv', './Data/DVWA_SQLInjection2.csv',
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

# SVM

# clf = LinearSVC(C=1, random_state=1, max_iter=100000)
# clf = SVC(C=1, random_state=1)
# clf.fit(X_train, Y_train)
# score_testing = clf.score(X_test, Y_test)
# print(score_testing)


y_GT = Y_test
Y_train = tf.keras.utils.to_categorical(Y_train, 2)
Y_test = tf.keras.utils.to_categorical(Y_test, 2)

# MLP (DAVID)
model = tf.keras.models.Sequential()
model.add(tf.keras.layers.Dense(64, activation='elu', input_shape=(X_train.shape[1],)))
model.add(tf.keras.layers.Dropout(0.5))
model.add(tf.keras.layers.Dense(32, activation='elu'))
model.add(tf.keras.layers.Dropout(0.5))
model.add(tf.keras.layers.Dense(16, activation='elu'))
model.add(tf.keras.layers.Dropout(0.5))

model.add(tf.keras.layers.Dense(2, activation='softmax'))

# CNN
# X_train = np.expand_dims(X_train,axis=2)
# X_test = np.expand_dims(X_test,axis=2)

# model = Sequential()
# model.add(Conv1D(16, kernel_size=5, input_shape=(X_train.shape[1], 1), padding='same', activation='elu'))
# model.add(Conv1D(32, kernel_size=5, padding='same', activation='elu'))
# model.add(MaxPooling1D(2))
# model.add(Dropout(0.5))
# model.add(Flatten())
# model.add(Dense(32, activation='elu'))
# model.add(Dropout(0.5))
# model.add(Dense(16, activation='elu'))
# model.add(Dropout(0.5))

# model.add(Dense(2, activation='softmax'))


model.compile(loss=tf.keras.losses.categorical_crossentropy,
              optimizer=Adam(),
              metrics=['accuracy'])
s_time = time.time()
history = model.fit(X_train, Y_train,
                    batch_size=256,
                    epochs=20,
                    verbose=1,
                    validation_data=(X_test, Y_test))
e_time = time.time()
print('Took {} seconds'.format(e_time - s_time))

ds = Dataset('./Data/wordPressNormalandAttack/NormalV1.1.csv', Normalize=True, Normalize_method='l2')
Y = tf.keras.utils.to_categorical(ds.Y, 2)
score = model.evaluate(ds.X, Y, verbose=1)
print(score)

from sklearn.metrics import roc_curve, auc, precision_recall_fscore_support
#y_pred = clf.predict(X_test)
y_pred = model.predict_classes(X_test)
# print y_pred

fpr, tpr , thres = roc_curve(y_GT,y_pred)
print(fpr)
print(tpr)
print(thres)
auc_s = auc(fpr,tpr)
print(auc_s)

# prec, recall, f_s, _ = precision_recall_fscore_support(y_GT,y_pred, average='micro')
# print prec, recall, f_s
