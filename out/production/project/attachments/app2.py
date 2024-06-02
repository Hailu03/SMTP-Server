import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

def plot2():
    # Read the CSV file
    my_data = pd.read_csv("health.csv")

    # Distribution of Gender
    plt.figure(figsize=(10, 6))
    sns.countplot(data=my_data, x='Gender', palette=['violet', 'purple'])
    plt.xlabel("Gender")
    plt.ylabel("Count")
    plt.title("Distribution of Gender")
    plt.show()

    # Density of Sleep Duration by Gender
    plt.figure(figsize=(10, 6))
    sns.kdeplot(data=my_data, x='Sleep Duration', hue='Gender', fill=True, alpha=0.5, palette=['violet', 'purple'])
    plt.xlabel("Sleep Duration")
    plt.ylabel("Density")
    plt.title("Density of Sleep Duration by Gender")
    plt.show()

    # Density of Sleep Duration by Gender and Stress Level
    g = sns.FacetGrid(my_data, col="Stress Level", col_wrap=3, height=4)
    g.map_dataframe(sns.kdeplot, x="Sleep Duration", hue='Gender', fill=True, alpha=0.5, palette=['violet', 'purple'])
    g.set_xlabels("Sleep Duration")
    g.set_ylabels("Density")
    g.fig.suptitle("Density of Sleep Duration by Gender and Stress Level")
    plt.subplots_adjust(top=0.85)
    
    return g.fig
