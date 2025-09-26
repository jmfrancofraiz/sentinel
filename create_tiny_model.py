#!/usr/bin/env python3
"""
Create a tiny TensorFlow Lite model for WhatsApp contact name extraction.
This model is extremely small for maximum compatibility.
"""

import tensorflow as tf
import numpy as np
import os

def create_tiny_contact_detection_model():
    """
    Create a very tiny neural network model for contact name extraction.
    Input: Text sequences (max 200 characters)
    Output: Contact names (max 50 characters)
    """
    
    # Model parameters - extremely small
    MAX_INPUT_LENGTH = 200
    MAX_OUTPUT_LENGTH = 50
    VOCAB_SIZE = 128  # Only basic ASCII
    
    # Input layer
    input_text = tf.keras.Input(shape=(MAX_INPUT_LENGTH,), name='input_text', dtype=tf.float32)
    
    # Very simple dense layers
    dense1 = tf.keras.layers.Dense(32, activation='relu', name='dense1')(input_text)
    dense2 = tf.keras.layers.Dense(16, activation='relu', name='dense2')(dense1)
    
    # Output layer - very small vocabulary
    output = tf.keras.layers.Dense(
        MAX_OUTPUT_LENGTH * VOCAB_SIZE,
        activation='softmax',
        name='output'
    )(dense2)
    
    # Reshape output
    output_reshaped = tf.keras.layers.Reshape(
        (MAX_OUTPUT_LENGTH, VOCAB_SIZE),
        name='output_reshaped'
    )(output)
    
    # Create model
    model = tf.keras.Model(inputs=input_text, outputs=output_reshaped, name='tiny_contact_detection_model')
    
    return model

def generate_tiny_training_data():
    """Generate very simple training data."""
    training_examples = [
        ("John Smith\nOnline", "John Smith"),
        ("Maria Garcia\nTyping", "Maria Garcia"),
        ("Dr. Johnson\nOnline", "Dr. Johnson"),
        ("Sarah Wilson\nLast seen", "Sarah Wilson"),
        ("Mike Chen\nOnline", "Mike Chen"),
    ]
    
    # Generate a few more examples
    names = ["Alex", "Chris", "Taylor", "Jordan", "Casey"]
    surnames = ["Anderson", "Taylor", "Thomas", "Jackson", "White"]
    
    for i in range(10):
        first_name = np.random.choice(names)
        last_name = np.random.choice(surnames)
        full_name = f"{first_name} {last_name}"
        text = f"{full_name}\nOnline"
        training_examples.append((text, full_name))
    
    return training_examples

def preprocess_text_tiny(text, max_length=200):
    """Convert text to simple character indices (basic ASCII only)."""
    char_indices = []
    for char in text[:max_length]:
        char_code = min(ord(char), 127)  # Only basic ASCII
        char_indices.append(char_code)
    
    # Pad to max_length
    while len(char_indices) < max_length:
        char_indices.append(0)
    
    return char_indices

def preprocess_contact_name_tiny(name, max_length=50):
    """Convert contact name to simple character indices (basic ASCII only)."""
    char_indices = []
    for char in name[:max_length]:
        char_code = min(ord(char), 127)  # Only basic ASCII
        char_indices.append(char_code)
    
    # Pad to max_length
    while len(char_indices) < max_length:
        char_indices.append(0)
    
    return char_indices

def create_training_data():
    """Create training data for the tiny model."""
    training_examples = generate_tiny_training_data()
    
    X = []
    y = []
    
    for text, contact_name in training_examples:
        text_indices = preprocess_text_tiny(text)
        X.append(text_indices)
        
        name_indices = preprocess_contact_name_tiny(contact_name)
        y.append(name_indices)
    
    return np.array(X, dtype=np.float32), np.array(y, dtype=np.float32)

def train_tiny_model():
    """Train the tiny contact detection model."""
    print("Creating tiny model...")
    model = create_tiny_contact_detection_model()
    
    print("Model architecture:")
    model.summary()
    
    print("Generating training data...")
    X_train, y_train = create_training_data()
    
    print(f"Training data shape: X={X_train.shape}, y={y_train.shape}")
    
    # Compile model
    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )
    
    # Train model with very few epochs
    print("Training tiny model...")
    history = model.fit(
        X_train, y_train,
        epochs=3,  # Very few epochs
        batch_size=2,  # Very small batch size
        validation_split=0.2,
        verbose=1
    )
    
    return model

def convert_to_tflite_tiny(model, output_path):
    """Convert the trained model to TensorFlow Lite format with maximum optimization."""
    print(f"Converting tiny model to TensorFlow Lite format...")
    
    # Convert to TensorFlow Lite
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Maximum optimization for size
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]
    
    # Convert
    tflite_model = converter.convert()
    
    # Save model
    with open(output_path, 'wb') as f:
        f.write(tflite_model)
    
    print(f"Tiny model saved to: {output_path}")
    print(f"Model size: {len(tflite_model) / 1024:.2f} KB")

def main():
    """Main function to create and train the tiny model."""
    print("WhatsApp Tiny Contact Detection Model Creator")
    print("=" * 50)
    
    # Create output directory
    output_dir = "app/src/main/assets/models"
    os.makedirs(output_dir, exist_ok=True)
    
    # Train tiny model
    model = train_tiny_model()
    
    # Convert to TensorFlow Lite
    tflite_path = os.path.join(output_dir, "contact_detection_model.tflite")
    convert_to_tflite_tiny(model, tflite_path)
    
    print("\nTiny model creation completed successfully!")
    print(f"TensorFlow Lite model saved to: {tflite_path}")

if __name__ == "__main__":
    main()
