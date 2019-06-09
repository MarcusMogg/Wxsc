import numpy as np
import os
import cv2
import matplotlib.pyplot as plt
import random
from PIL import Image
import sys

def read_image(filename, resize_height, resize_width, normalization=False):
    bgr_image = cv2.imread(filename)
    if len(bgr_image.shape) == 2:  # 若是灰度图则转为三通道
        print("Warning:gray image", filename)
        bgr_image = cv2.cvtColor(bgr_image, cv2.COLOR_GRAY2BGR)

    rgb_image = cv2.cvtColor(bgr_image, cv2.COLOR_BGR2RGB)  # 将BGR转为RGB
    if resize_height > 0 and resize_width > 0:
        rgb_image = cv2.resize(rgb_image, (resize_width, resize_height))
    rgb_image = np.asanyarray(rgb_image)
    if normalization:
        # 不能写成:rgb_image=rgb_image/255
        rgb_image = rgb_image / 255.0
    # show_image("src resize image",image)
    return rgb_image


if __name__ == '__main__':
    a = read_image(sys.argv[1], 224, 224, 'normalization')
    for i  in a:
        for j in i:
            for k in j:
                print(k)