# 文件系统 
19302010074 张诗涵
## 理解
- block存储实际块中内容，而每个bm管理其下的block，而一个具体的file可能需要依赖多个bm下的不同块，文件最终存储的其实是对不同的block的引用顺序，按顺序将block中的内容拼接起来其实就得到了文件的完整内容。
<br>而fm就类似于不同的用户，每个用户各自会创建、读写文件，且此过程不会与别的用户有干涉，这也就解释了为什么复制文件时应该存储在原文件所在的fm下。
- 使用引用记录每个文件使用了哪些blk的打包，进行文件复制后，不管复制几份、相同内容的部分只需要一份文件的空间存储即可，减少了对存储空间的占有
- 插入文件内容更加简便——不用修改blk的内容，而只需要新建一组blk作为新内容的存储者，同时根据插入位置对filemeta中记录的块引用顺序进行修改，即完成了文件整体内容的修改。
- duplicate的意义：保证任意单一block的损坏不会影响整体文件的读写，但是duplicate块的存在意味着单一文件的存储成本是其原数据大小的n倍，因此似乎只有在存储频繁出现/内容类似的大文件时有比较大的实际意义。
## 设计
### Block Level
文件夹路径： FileSystem/Block
- Block Manager: 文件夹路径：FileSystem/Block/bm-x(x为bmId)，类中以Arraylist形式保存其下管理的所有Block对象。恢复结构时根据读到的x创建，x即代表其bmId，同时遍历其文件夹下存储的所有meta文件生成对应的Block对象并存储。<br>
- Block: 文件夹路径：FileSystem/Block/bm-x/y.data(实际内容);FileSystem/Block/bm-x/y.meta(元数据)。根据创建内容(实际byte[]数组或size)调用不同的构造参数，保存id、bm等信息，并计算checksum、写入data。等到duplicate的剩下几个block也创建完毕后，再统一写入meta信息
- 值得注意: Block与BlockManager实际的id与其在arraylist中的下标并不一定一致，如一个bm被删除后又被重建，则下标将为最后一个，但记录在filemeta中的是他原始的id，因此每次寻找的时候都需要根据id进行遍历查找。
### FileLevel
- File Manager: 文件夹路径：FileSystem/File/fm-x(x为fmId)，类中同样以ArrayList形式存储其管理的files。
- File: 文件夹路径：FileSystem/File/fm-x/y.meta，内部构建了write、read、move等与外界交互的函数，同时构建了工具函数，例如getCorrectBlockOfLevel会根据具体要第几层block进行查找，并根据实际情况(例如有块被删除、bm被删除、内容改动等)进行对duplicate blocks的查找，并尝试对坏块进行修复。
最后返回内容正确的Block对象。<br>
类似的，也有generateBlocks(byte[] source)函数根据调取FileSystem的partition获取随机的bm并在对应bm下生成Block，最终返回给file生成好的一组duplicate blocks。
### FileSystem Level
- 静态字段: 以ArrayList形式存储
- 静态方法: <br>
**initialize():** 初始化系统，会尝试根据FileSystem文件夹及其下的文件进行回复对象的尝试，如果失败则按默认加入5个bm与3个fm
<br>
**各类get方法:** 由于fmId与其在fms列表内的下标不一定相同，同时fileId全局计数、因此需要由System在全部fm中进行排查以检查其是否存在。
<br>
**partition(int num):** 随机获取num个不同的bm返回，用于file写入时需要创建一个logicBlock时使用。
## 亮点
- 文件结构清晰，通过持久化形成的文件结构可以直观地理解文件系统的结构、类之间的关系等设计思路。
- 函数设计：例如文件写入时只需要传入byte[]内容即返回n个写好的block; 上下层间的良好隔离使得我在发现自己write的理解有偏差时能较快地、且不需要改动下层的方式进行调整。
- 虽然没有做自动检测修复，但是做了被动检测，即在试图查看某个block时如果发现其损坏，会在得到正确数据后尝试恢复(包括重建整个bm、重建整个data、将被修改过的data内容改回正确值等)<br>
我认为这是有意义的，虽然比较简单，一方面，坏掉的块早于好的块被找到，说明其在文件的引用表中较前处的位置，如果在发现损坏后不做任何处理，那么此后每次读写都需要遭受同等的多次碰到坏块的问题；
而修改本身并不带来过多的开销，相反一个坏块的及时处理有助于避免文件系统出现由于所有duplicate块都损坏导致的文件彻底损坏。
- 图形化界面简洁、用户友好，易于操作，例如点击查看文件内容，修改文件内容的界面更清晰、有所对照等符合人平时的使用习惯。


