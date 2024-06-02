import plotly.express as px
import pandas as pd

def plot1(jobs):
    # Read the CSV file
    my_data = pd.read_csv("health.csv")

    # Filter data for the specified occupations
    filtered_data = my_data[my_data['Occupation'].isin(jobs)]

    # Create the plot
    fig = px.bar(filtered_data,
                 x='Gender',
                 y='Sleep Duration',
                 color='Occupation',
                 barmode='group',  # Group bars for comparison
                 category_orders={'Gender': ['Male', 'Female']},
                 color_discrete_sequence=px.colors.qualitative.Plotly,
                 height=600,        # Adjust height for better display
                 width=800)         # Adjust width for better display

    # Update the layout
    fig.update_layout(
        title=f'Relationship between Gender and Sleep Duration for {", ".join(jobs)}',
        xaxis_title='Gender',
        yaxis_title='Average Sleep Duration (hours)',
        margin=dict(t=50),
        font=dict(size=12),
        legend_title_text='Occupation'
    )

    # Update traces for better aesthetics
    fig.update_traces(
        marker=dict(line=dict(width=0.5, color='DarkSlateGrey')),
        opacity=0.8
    )

    # Adjust the x-axis and y-axis to make the plots more readable
    fig.update_xaxes(tickfont=dict(size=14))
    fig.update_yaxes(tickfont=dict(size=14))

    return fig