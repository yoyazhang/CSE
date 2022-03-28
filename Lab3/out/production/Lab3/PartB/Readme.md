# Part B实验文档
19302010074 张诗涵
## 实现思路
### update方式
- 考虑到ppt中也有提到，不要对存储文件的唯一副本进行修改，我认为在修改时不应该直接修改database文件，而是应该选择开一个新的副本(或首先在内存中修改)，
例如在"#db1"中修改，如果能commit就用副本替代原件(如果是内存中写的话，则是将其写入db文件覆盖内容); 如果abort处理也更加简单，放弃这份副本即可
(内存中更新的话更简单、直接不处理就好了)
- 这样保证了在未commit前不会影响到原件，即使是后来的修改者read到的也是最新一版被commit的数据，而不会出现读到了之前修改者实际并未commit的数据
### Write-Ahead-Log
- 理解：wal主要是为了给那些做到一半尚未commit也未abort的事务~~擦屁股~~善后(即没有outcome类log的事务), 因为对于那些有结果的事务，它们都已经完成
了自己的责任，如果是需要abort就已经完成了回滚的义务, 只有那些中途断电的自己无法回滚，只好由recover来帮助完成回滚、并为其添加outcome+abort的log
- 结构：LogFile类用ArrayList管理迄今为止的所有Log(即Log类对象),Log类记录了type(BEGIN,CHANGE,OUTCOME三类),operation
(NEW_TRANSACTION/ABORT/COMMIT/PUT等),workId(即watermark),choices(选项字段，主要是put操作需要记录是将第几行从什么值修改为了什么值)
- 记录：在MyAtomicity的每一个操作(开始更新、更新某行、结束)之前都要进行Log的记录，记录本次操作type、watermark、operation, 如果是put则还需要
选项字段(详细内容及结构见上)，从保存的文件恢复LogFile对象，使用提供的addLog接口插入新Log，LogFile类会在内部调用save()函数将本次结果保存进磁盘。
- Recover：从保存的文件恢复LogFile对象，从最后向前遍历，如果该log type为OUTCOME则将其加入finishIds, 并对每条判断如果其不在finishId中，且type为CHANGE
就要进行回滚,同时如果它还不在loserId(所有中断事务的集合)中的话就将其加入。最后对loserId进行遍历，为其中每条都添加OUTCOME(ABORT)记录表示
已经完成回滚善后
### Read-Capture
- 理解：read-capture是一种乐观锁的理念，即认为大概率不会出现干扰。
- 运行的主线程中管理着watermark的静态全局变量，每个atom试图进行修改操作时都必须获取一个watermark
- 获取watermark的方式是将当前watermark + 1并返回
- 任何update操作最后提交前需要比较自身watermark是否与当前最高watermark一致，若一致则commit，不一致则abort
- commit时就正式打开db文件覆盖其原内容
- abort时就放弃本次所有内存内修改部分，同时休眠一段时间(我设置为了9000ms)然后再重新调用自身再尝试写入。
- 强制休眠一段时间是因为之前abort一定是和一个新修改者冲突了，如果此时立刻retry，则大概率自己又会干扰到对方、对方再次retry干扰自己……
造成互相钳制，因此一次abort后必须进行休眠。
## 与提供模板的不同
- 我将recover函数搬到了用于总控的Controller类中，因为个人理解wal是为了保证all-or-nothing，而假设未曾断电，则每个正在修改的atom都有职责
保证自身事务的原子性，因此不可能出现一个事务做到一半而没有结果的情况，只有中途断电，这个事务无法自己完成回滚，于是只好在下次运行时通过
log的记录来补足回滚的步骤、完成原子性。
- 将write(int line, char ch)移除了，因为我们实际上没有对某行单独修改的操作,且在内存中修改也可以通过sleep完成强制休眠1000ms的要求，
没有集成出一个函数的必要。
- 理论上recover()应该在运行后自动调用，但是为了测试断电后恢复前后log的区别，采用了输入指令调用的方式
## 测试过程&截图
- 运行Controller类的main函数即可开始操作

- 输入的指令格式为\<command> <atom_id> <to_write_ch>(第一个为操作， 第二个为要使用的atom号，第二个为要写入的character)

- 首先测试能否正确更新

- ![console_info](C:\Users\admin\AppData\Roaming\Typora\typora-user-images\image-20211224140328098.png)

- ![db_change_to_2](C:\Users\admin\AppData\Roaming\Typora\typora-user-images\image-20211224140402350.png)

- 然后测试两个线程并发时前者是否放弃，又是否成功重做

- ![image-20211224140932147](C:\Users\admin\AppData\Roaming\Typora\typora-user-images\image-20211224140932147.png)

  ![image-20211224141000525](C:\Users\admin\AppData\Roaming\Typora\typora-user-images\image-20211224141000525.png)

  ![image-20211224141019790](C:\Users\admin\AppData\Roaming\Typora\typora-user-images\image-20211224141019790.png)

- 最后测试是否断电后会回滚上次中断的事务(上图中可看到最后一次更新未到结果即断电)

- 未recover前

- 

- recover后

- 
