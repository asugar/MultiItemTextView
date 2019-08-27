package com.foolchen.lib.multiitemtextview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * 可以显示多个项目的TextView
 * @author chenchong
 * 2017/12/3 0003
 * 16:04
 */
open class MultiItemTextView : View {
  private val GRAVITY_NONE = 0
  private val GRAVITY_CENTER_VERTICAL = 1
  private val GRAVITY_CENTER = 2

  // 要显示的item的数量，如果为-1则按照传入的item的数量显示
  private var mItemCount = -1
  // 文本显示的颜色，默认为灰色
  private var mTextColor = Color.GRAY
  // 文字大小，默认为24px
  private var mTextSize = 24F

  // item的宽高，如果未设置则根据文本变化
  private var mItemWidth = -1F
  private var mItemHeight = -1F
  // 宽度的参考值，在设置了该值后，计算高度时会有限参考该值；否则会根据View的实际宽高来计算
  private var mRefWidth = -1F
  // 是否根据条目数量平分宽度
  private var mDividerEqually = false
  private var mDividerColor = Color.GRAY
  private var mDividerWidth = 0F
  private var mStartDividerEnable = false
  private var mTopDividerEnable = false
  private var mEndDividerEnable = false
  private var mBottomDividerEnable = false
  private var mMiddleDividerEnable = false
  private var UnderlineTextmEnable = false
  private var mGravity = 0

  // 每个item的内边距
  private val mItemPadding = IntArray(4)

  private val mBound = Rect()
  private val mPaint = Paint()

  private val mItems = ArrayList<String>()
  private val mShadowItems = ArrayList<String>()
  // 分割后的字符串，用于绘制多行文本
  private val mDividedItems = ArrayList<List<String>>()
  // 该列表中的元素仅用于计算高度（mRefWidth!=-1F时）
  private val mRefDividedItems = ArrayList<List<String>>()

  constructor(context: Context?) : super(context) {
    init(context, null)
  }

  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    init(context, attrs)
  }

  fun setTexts(vararg items: String) {
    setTexts(items.asList())
  }

  fun setTexts(items: List<String>) {
    mItems.clear()
    mItems.addAll(items)
    mShadowItems.clear()
    mShadowItems.addAll(mItems)
    ensureItems()
    requestLayout()
    invalidate()
  }

  fun getItems(): List<String> {
    return mItems
  }

  fun setItemCount(itemCount: Int) {
    mItemCount = itemCount
    requestLayout()
    invalidate()
  }

  fun getItemCount(): Int {
    return mItemCount
  }

  /** 设置文本大小，单位为px */
  fun setTextSize(textSize: Float) {
    mTextSize = textSize
    mPaint.textSize = mTextSize
    requestLayout()
    invalidate()
  }

  fun getTextSize() = mTextSize

  /** 设置item宽度 */
  fun setItemWidth(width: Float) {
    mItemWidth = width
    requestLayout()
    invalidate()
  }

  fun getItemWidth() = mItemWidth

  fun setItemHeight(height: Float) {
    mItemHeight = height
    requestLayout()
    invalidate()
  }

  fun getItemHeight() = mItemHeight

  fun setRefWidth(width: Float) {
    mRefWidth = width
    requestLayout()
    invalidate()
  }

  fun setDividerColor(color: Int) {
    mDividerColor = color
    invalidate()
  }

  fun getDividerColor() = mDividerColor

  fun setDivideWidth(width: Float) {
    mDividerWidth = width
    requestLayout()
    invalidate()
  }

  fun setDividerEnable(start: Boolean, top: Boolean, end: Boolean, bottom: Boolean,
      middle: Boolean) {
    mStartDividerEnable = start
    mTopDividerEnable = top
    mEndDividerEnable = end
    mBottomDividerEnable = bottom
    mMiddleDividerEnable = middle
    requestLayout()
    invalidate()
  }

  fun setGravity(gravity: Int) {
    mGravity = gravity
    invalidate()
  }

  fun getGravity() = mGravity

  fun setItemPadding(start: Int, top: Int, end: Int, bottom: Int) {
    mItemPadding[0] = start
    mItemPadding[1] = top
    mItemPadding[2] = end
    mItemPadding[3] = bottom
    requestLayout()
    invalidate()
  }

  fun setUnderlineTextEnable(isUnderlineText: Boolean) {
    UnderlineTextmEnable = isUnderlineText
  }

  private fun init(context: Context?, attrs: AttributeSet?) {
    if (context != null && attrs != null) {
      val ta = context.obtainStyledAttributes(attrs, R.styleable.MultiItemTextView)

      mItemCount = ta.getInt(R.styleable.MultiItemTextView_mitv_count, -1)
      mTextColor = ta.getColor(R.styleable.MultiItemTextView_mitv_color, Color.GRAY)
      mTextSize = ta.getDimension(R.styleable.MultiItemTextView_mitv_text_size, 24F)
      mItemWidth = ta.getDimension(R.styleable.MultiItemTextView_mitv_item_width, -1F)
      mItemHeight = ta.getDimension(R.styleable.MultiItemTextView_mitv_item_height, -1F)
      mRefWidth = ta.getDimension(R.styleable.MultiItemTextView_mitv_ref_item_width, -1F)
      mDividerColor = ta.getColor(R.styleable.MultiItemTextView_mitv_divider_color,
          Color.GRAY)
      mDividerWidth = ta.getDimension(R.styleable.MultiItemTextView_mitv_divider_width, 1F)
      mStartDividerEnable = ta.getBoolean(R.styleable.MultiItemTextView_mitv_start_divider_enable,
          false)
      mTopDividerEnable = ta.getBoolean(R.styleable.MultiItemTextView_mitv_top_divider_enable,
          false)
      mEndDividerEnable = ta.getBoolean(R.styleable.MultiItemTextView_mitv_end_divider_enable,
          false)
      mBottomDividerEnable = ta.getBoolean(R.styleable.MultiItemTextView_mitv_bottom_divider_enable,
          false)
      mMiddleDividerEnable = ta.getBoolean(R.styleable.MultiItemTextView_mitv_middle_divider_enable,
          false)
      mGravity = ta.getInt(R.styleable.MultiItemTextView_mitv_gravity, 0)
      val padding = ta.getDimensionPixelSize(R.styleable.MultiItemTextView_mitv_item_padding, 0)
      mItemPadding.fill(padding, 0, mItemPadding.size)
      ta.getDimensionPixelSize(
          R.styleable.MultiItemTextView_mitv_item_padding_start, 0).let {
        if (it != 0) {
          mItemPadding[0] = it
        }
      }
      ta.getDimensionPixelSize(
          R.styleable.MultiItemTextView_mitv_item_padding_top, 0).let {
        if (it != 0) {
          mItemPadding[1] = it
        }
      }
      ta.getDimensionPixelSize(
          R.styleable.MultiItemTextView_mitv_item_padding_end, 0).let {
        if (it != 0) {
          mItemPadding[2] = it
        }
      }
      ta.getDimensionPixelSize(
          R.styleable.MultiItemTextView_mitv_item_padding_bottom, 0).let {
        if (it != 0) {
          mItemPadding[3] = it
        }
      }
      val text = ta.getString(R.styleable.MultiItemTextView_mitv_texts)
      text?.let {
        val items = text.split("|")
        if (mItemCount < items.size) {
          mItemCount = items.size
        }
        mShadowItems.addAll(items)
        ensureItems()
      }
      ta.recycle()
    }

    mPaint.isAntiAlias = true
    mPaint.style = Paint.Style.FILL
    mPaint.textSize = mTextSize
    //mPaint.getTextBounds(mText, 0, mText.length, mBound)

  }

  /**该方法用于保证要绘制的条目与设置的条目数量相同*/
  private fun ensureItems() {
    while (mShadowItems.size < mItemCount) {
      mShadowItems.add("")
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val width = measureSelf(widthMeasureSpec, true)
    val height = measureSelf(heightMeasureSpec, false)
    setMeasuredDimension(width, height)
  }


  override fun onDraw(canvas: Canvas) {
    mPaint.color = mTextColor

    // 计算绘制文本的起始位置
    //val startX = width / 2F - mBound.width() / 2F
    // 绘制文本时从文本的左下角开始，故如果要使文本居中，则开始位置需要+fontSize * 0.5f
    // 而此处绘制时不应该包含字体本身的边距，故使用Rect来获取高度
    //val startY = height / 2F + mBound.height() / 2F
    // 绘制文本
    //canvas.drawText(mText, startX, startY, mPaint)

    drawItems(canvas)

    if (isInEditMode) {
      // 绘制中线，用于查看文本的位置
      mPaint.color = Color.GRAY
      mPaint.strokeWidth = 5F
      canvas.drawLine(0F, height / 2F, width.toFloat(), height / 2F, mPaint)
    }
  }

  // 绘制各个条目
  private fun drawItems(canvas: Canvas) {
    val itemWidth = calItemWidth(width.toFloat())
    val top = calTop()
    val bottom = calBottom()
    val displayFontSize = getDisplayFontSize()
    for (i in 0 until mItemCount) {
      val start = calStart(i, itemWidth)
      val end = start + itemWidth

      val drawableStart = start + mItemPadding[0]
      val drawableTop = top + mItemPadding[1]
      val drawableEnd = end - mItemPadding[2]
      val drawableBottom = bottom - mItemPadding[3]
      if (isInEditMode) {
        // 绘制整个条目的区域
        mPaint.color = if (i % 2 == 0) Color.BLUE else Color.YELLOW
        mPaint.style = Paint.Style.FILL
        canvas.drawRect(start, top, end, bottom, mPaint)

        // 绘制内容区域
        mPaint.color = Color.RED
        mPaint.strokeWidth = 1F
        mPaint.style = Paint.Style.STROKE
        canvas.drawRect(drawableStart, drawableTop, drawableEnd, drawableBottom, mPaint)
      }

      // 为了防止文本被覆盖，在绘制背景后绘制文本
      val texts = mDividedItems[i]
      val size = texts.size
      var offsetY = 0F
      if (mGravity == GRAVITY_CENTER_VERTICAL || mGravity == GRAVITY_CENTER) {
        offsetY = (drawableBottom - drawableTop - displayFontSize * size) / 2F
        if (offsetY < 0F) {
          offsetY = 0F
        }
      }
      for (textIndex in 0 until size) {
        val text = texts[textIndex]
        val startY = drawableTop + textIndex * displayFontSize + mTextSize// 由于文本从左下角开始绘制，故其开始位置
        var offsetX = 0F
        if (mGravity == GRAVITY_CENTER) {
          mPaint.getTextBounds(text, 0, text.length, mBound)
          offsetX = (drawableEnd - drawableStart - mBound.width()) / 2F
        }
        mPaint.color = mTextColor
        mPaint.textSize = mTextSize
        mPaint.strokeWidth = 0F
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.isUnderlineText = UnderlineTextmEnable
        canvas.drawText(text, drawableStart + offsetX, startY + offsetY, mPaint)
      }

      if (mDividerWidth > 0) {
        mPaint.color = mDividerColor
        mPaint.strokeWidth = mDividerWidth
        mPaint.style = Paint.Style.FILL
        if (mStartDividerEnable) {
          canvas.drawRect(0F, 0F, mDividerWidth, height.toFloat(), mPaint)
        }
        if (mTopDividerEnable) {
          canvas.drawRect(0F, 0F, width.toFloat(), top, mPaint)
        }
        if (mEndDividerEnable) {
          canvas.drawRect(width - mDividerWidth, 0F, width.toFloat(), height.toFloat(), mPaint)
        }
        if (mBottomDividerEnable) {
          canvas.drawRect(0F, bottom, width.toFloat(), bottom + mDividerWidth, mPaint)
        }
        if (mMiddleDividerEnable) {
          // 绘制分隔线
          canvas.drawRect(end, top, end + mDividerWidth, bottom, mPaint)
        }
      }
    }
  }

  /**
   * 测量当前View宽高的方法
   */
  private fun measureSelf(spec: Int, isWidth: Boolean): Int {
    val oldSize = MeasureSpec.getSize(spec).toFloat()
    var newSize = 0F
    when (MeasureSpec.getMode(spec)) {
      MeasureSpec.EXACTLY -> {
        // 需要精确高度，则直接使用原先定义的大小即可
        newSize = oldSize
      }
      MeasureSpec.AT_MOST -> {
        // 指定了match_parent
        newSize = if (isWidth) {
          oldSize
        } else {
          calItemHeight()
        }
      }
      MeasureSpec.UNSPECIFIED -> {
        // 指定了wrap_content
        newSize = if (isWidth) {
          if (mItemWidth == -1F) {
            // 未指定每个条目的宽度，则无法计算总宽度
            throw IllegalArgumentException("在View的宽度未精确指定时，需要指定每个条目的宽度(mitv_item_width)")
          }
          paddingLeft + mItemCount * mItemWidth + paddingRight
        } else {
          calItemHeight()
        }
      }
    }
    if (isWidth) {
      divideItems(newSize)
    }
    return newSize.toInt()
  }

  private fun getDisplayFontSize(): Int {
    val fm = mPaint.fontMetricsInt
    return fm.bottom - fm.top
  }

  // 计算每个条目的宽度
  private fun calItemWidth(width: Float): Float {
    return if (mItemWidth == -1F) {
      (width - paddingLeft - paddingRight - (if (mMiddleDividerEnable) mDividerWidth * (mItemCount - 1) else 0F) - (if (mStartDividerEnable) mDividerWidth else 0F) - (if (mEndDividerEnable) mDividerWidth else 0F)) / mItemCount.toFloat()
    } else {
      mItemWidth
    }
  }

  // 根据当前条目的位置，计算其起始位置
  private fun calStart(index: Int, itemWidth: Float): Float {
    return paddingLeft + index * (itemWidth + if (mMiddleDividerEnable) mDividerWidth else 0F) + if (mStartDividerEnable) mDividerWidth else 0F
  }

  private fun calTop(): Float {
    return paddingTop.toFloat() + if (mTopDividerEnable) mDividerWidth else 0F
  }

  private fun calBottom(): Float {
    return (height - paddingBottom).toFloat() - if (mBottomDividerEnable) mDividerWidth else 0F
  }

  /** 计算条目的高度（在未指定高度的情况下） */
  private fun calItemHeight(): Float {
    return if (mItemHeight == -1F) {
      var lines = 0
      (if (mRefWidth == -1F) mDividedItems else mRefDividedItems)
          .asSequence()
          .filter { it.size > lines }
          .forEach { lines = it.size }
      paddingTop + paddingBottom + mItemPadding[1] + mItemPadding[3] + lines * getDisplayFontSize().toFloat() + (if (mTopDividerEnable) mDividerWidth else 0F) + (if (mBottomDividerEnable) mDividerWidth else 0F)
    } else {
      mItemHeight
    }
  }

  /** 分割各个item */
  private fun divideItems(width: Float) {
    // 每次重新分割item前都将已经分割的清空
    mDividedItems.clear()
    // 首先需要计算可绘制的最大宽度

    val drawableWidth = calItemWidth(width) - mItemPadding[0] - mItemPadding[2]

    if (drawableWidth > 0) {
      // 需要保证每一行的宽度都小于drawableWidth
      mShadowItems.mapTo(mDividedItems) { divideItem(drawableWidth, it) }
      if (mRefWidth != -1F) {
        mShadowItems.mapTo(mRefDividedItems) {
          divideItem(mRefWidth - paddingLeft - paddingRight - mItemPadding[0] - mItemPadding[2], it)
        }
      }
    }
  }

  /** 分割特定的item */
  private fun divideItem(drawableWidth: Float, item: String): List<String> {
    val texts = ArrayList<String>()
    var text = item
    var end = text.length
    while (true) {
      mPaint.getTextBounds(text, 0, end, mBound)
      if (mBound.width() <= drawableWidth) {
        // 宽度小于可绘制宽度，则添加到列表中
        texts.add(text.substring(0, end))
        text = text.substring(end)
        end = text.length
        if (text.isEmpty()) break// 没有需要继续处理的字符串时，跳出循环
      } else {
        // 宽度大于可绘制宽度，则缩减上限
        end--
      }
    }
    return texts
  }
}