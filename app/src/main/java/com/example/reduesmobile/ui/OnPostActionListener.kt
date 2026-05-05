package com.example.reduesmobile.ui

import com.example.reduesmobile.data.dto.PublicacionResponse

interface OnPostActionListener {
    fun onUserNameClick(post: PublicacionResponse?, position: Int)
    fun onLikeClick(post: PublicacionResponse?, position: Int)
    fun onSaveClick(post: PublicacionResponse?, position: Int)
    fun onCommentClick(post: PublicacionResponse?, position: Int)
}